package com.example.yourroom.location

import android.content.Context
import com.example.yourroom.R
import org.json.JSONObject
import java.text.Normalizer

// ---------------------------------------------------------------------
// REPOSITORIO DE MUNICIPIOS (CARGA + BÚSQUEDA LOCAL)
// ---------------------------------------------------------------------

/**
 * Carga un dataset de municipios desde un recurso JSON local (raw/municipios_es)
 * y ofrece una búsqueda rápida (case/diacritics-insensitive) sobre una lista
 * preparada para la UI.
 *
 * Notas:
 * * - Se calcula y cachea una lista "UI" con etiqueta amigable y un campo
 *   pre-normalizado para acelerar las búsquedas.
 * - La búsqueda no re-ordena toda la lista: prioriza "empieza por" y luego "contiene".
 */
object MunicipiosRepository {

    // -----------------------------------------------------------------
    // MODELO PARA LA UI
    // -----------------------------------------------------------------

    /**
     * Representa un municipio listo para pintar en la UI.
     *
     * @param label Etiqueta amigable: "Elche (Alicante/Alacant)".
     * @param name Nombre del municipio ya formateado (sin artículos finales).
     * @param provinceName Nombre visible de la provincia (map por código).
     * @param provinceCode Código de provincia de 2 dígitos (ej: "03").
     * @param code Código de municipio si está presente en el dataset (puede ser vacío).
     * @param normLabel Versión pre-normalizada de label (sin tildes y minúsculas),
     *                  usada para búsquedas rápidas sin recalcular en cada item.
     */
    data class MunicipioUi(
        val label: String,
        val name: String,
        val provinceName: String,
        val provinceCode: String,
        val code: String,
        val normLabel: String
    )

    /** Cachea la lista UI procesada para evitar releer/parsear el JSON. */
    private var cacheUi: List<MunicipioUi>? = null

    // -----------------------------------------------------------------
    // CARGA + PREPROCESADO
    // -----------------------------------------------------------------

    /**
     * Lee el JSON de municipios del recurso raw y lo transforma en una lista
     * de [MunicipioUi] con campos listos para pintar y buscar.
     *
     * - Detecta dinámicamente los índices de las columnas (por nombres posibles).
     * - Aplica `prettyName` para ordenar artículos finales ("..., el/la/los/las/l'").
     * - Mapea código de provincia → nombre.
     * - Pre-normaliza `label` para búsquedas rápidas (campo `normLabel`).
     * - Elimina duplicados por `label` (por si el dataset trae variantes).
     *
     * El resultado se cachea en memoria.
     */
    fun getUiList(context: Context): List<MunicipioUi> {
        cacheUi?.let { return it }

        val txt = context.resources.openRawResource(R.raw.municipios_es)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(txt)

        // 1) Localiza índices de columnas (dataset Socrata)
        val cols = root.getJSONObject("meta").getJSONObject("view").getJSONArray("columns")
        val idxByField = HashMap<String, Int>(cols.length())
        for (i in 0 until cols.length()) {
            cols.getJSONObject(i).optString("fieldName")?.lowercase()?.let { f ->
                if (f.isNotBlank()) idxByField[f] = i
            }
        }
        fun pick(vararg c: String) = c.firstNotNullOfOrNull { idxByField[it.lowercase()] }

        val idxName = pick("nom", "municipio", "name", "municipi", "nombre")
            ?: error("No se encontró columna de nombre de municipio")
        val idxProvCode = pick(
            "codi_prov_ncia", "cpro", "codigo_provincia", "cod_prov", "provincia_codigo"
        ) ?: error("No se encontró columna de código de provincia")
        val idxCode = pick("codi", "codigo", "cmum", "cod_mun", "codigo_municipio") ?: -1

        // 2) Construye lista UI + pre-normaliza
        val rows = root.getJSONArray("data")
        val out = ArrayList<MunicipioUi>(rows.length())
        for (r in 0 until rows.length()) {
            val row = rows.getJSONArray(r)
            val rawName = row.optString(idxName)
            val name = prettyName(rawName)
            val provCode = row.optString(idxProvCode).padStart(2, '0')
            val provName = PROVINCE_BY_CODE[provCode] ?: provCode
            val code = if (idxCode >= 0) row.optString(idxCode) else ""
            val label = "$name ($provName)"
            out.add(
                MunicipioUi(
                    label = label,
                    name = name,
                    provinceName = provName,
                    provinceCode = provCode,
                    code = code,
                    normLabel = norm(label) // <- PRE-NORMALIZADO
                )
            )
        }

        // Elimina duplicados por label (por si el dataset trae variantes)
        val dedup = out.distinctBy { it.label }
        cacheUi = dedup
        return dedup
    }

    // -----------------------------------------------------------------
    // BÚSQUEDA RÁPIDA (UX primero: empieza-por, luego contiene)
    // -----------------------------------------------------------------

    /**
     * Filtra una lista de municipios usando una query normalizada.
     *
     * Regla de ranking:
     * 1) Primero resultados cuyo `normLabel` EMPIEZA por la query.
     * 2) Si faltan resultados, completa con los que solo CONTIENEN la query.
     *
     * @param all Lista previamente generada por [getUiList].
     * @param query Texto introducido por el usuario (se normaliza internamente).
     * @param limit Número máximo de resultados a devolver (por defecto 10).
     */
    fun filter(all: List<MunicipioUi>, query: String, limit: Int = 10): List<MunicipioUi> {
        val q = norm(query).trim()
        if (q.length < 2) return emptyList()

        // 1º: los que empiezan por la query (mejor UX), 2º: los que solo contienen
        val starts = ArrayList<MunicipioUi>(limit)
        val contains = ArrayList<MunicipioUi>(limit)

        for (m in all) {
            val idx = m.normLabel.indexOf(q)
            if (idx == 0) {
                starts.add(m)
                if (starts.size >= limit) break
            } else if (idx > 0) {
                contains.add(m)
            }
        }
        // Completa con "contains" si hacen falta más resultados
        if (starts.size < limit && contains.isNotEmpty()) {
            val need = limit - starts.size
            starts.addAll(contains.take(need))
        }
        return starts
    }

    // -----------------------------------------------------------------
    // HELPERS: FORMATEO + NORMALIZACIÓN
    // -----------------------------------------------------------------

    /**
     * Reordena nombres con artículo final para mostrarlos como
     * "El/La/Los/Las/L' <Nombre>" en vez de "<Nombre>, el/la/los/las/l'".
     *
     * Ej: "Pobla de Farnals, la" → "La Pobla de Farnals"
     */
    private fun prettyName(raw: String): String {
        val m = Regex("^(.+),\\s*(el|la|los|las|l')$", RegexOption.IGNORE_CASE).find(raw.trim())
        return if (m != null) {
            m.groupValues[2].replaceFirstChar { it.titlecase() } + " " + m.groupValues[1]
        } else raw.trim()
    }

    /**
     * Normaliza strings para comparación/búsqueda:
     * - Pasa a NFD y elimina marcas diacríticas (tildes).
     * - Convierte a minúsculas.
     * - Facilita búsquedas "sin tildes" y case-insensitive.
     */
    private fun norm(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()

    // -----------------------------------------------------------------
    // MAPA CÓDIGO→PROVINCIA (NOMBRES VISIBLES)
    // -----------------------------------------------------------------

    /** Mapa estático del código de provincia (2 dígitos) a su nombre visible. */
    private val PROVINCE_BY_CODE = mapOf(
        "01" to "Álava/Araba","02" to "Albacete","03" to "Alicante/Alacant","04" to "Almería",
        "05" to "Ávila","06" to "Badajoz","07" to "Illes Balears","08" to "Barcelona",
        "09" to "Burgos","10" to "Cáceres","11" to "Cádiz","12" to "Castellón/Castelló",
        "13" to "Ciudad Real","14" to "Córdoba","15" to "A Coruña","16" to "Cuenca",
        "17" to "Girona","18" to "Granada","19" to "Guadalajara","20" to "Gipuzkoa",
        "21" to "Huelva","22" to "Huesca","23" to "Jaén","24" to "León","25" to "Lleida",
        "26" to "La Rioja","27" to "Lugo","28" to "Madrid","29" to "Málaga","30" to "Murcia",
        "31" to "Navarra","32" to "Ourense","33" to "Asturias","34" to "Palencia",
        "35" to "Las Palmas","36" to "Pontevedra","37" to "Salamanca","38" to "Santa Cruz de Tenerife",
        "39" to "Cantabria","40" to "Segovia","41" to "Sevilla","42" to "Soria","43" to "Tarragona",
        "44" to "Teruel","45" to "Toledo","46" to "Valencia/València","47" to "Valladolid",
        "48" to "Bizkaia","49" to "Zamora","50" to "Zaragoza","51" to "Ceuta","52" to "Melilla"
    )
}
