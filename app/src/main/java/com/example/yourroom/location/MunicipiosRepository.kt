package com.example.yourroom.location

import android.content.Context
import com.example.yourroom.R
import org.json.JSONObject
import java.text.Normalizer

object MunicipiosRepository {

    // --- Modelo con etiqueta "bonita" y versión pre-normalizada para búsquedas ---
    data class MunicipioUi(
        val label: String,      // "Elche (Alicante/Alacant)"
        val name: String,       // "Elche"
        val provinceName: String,
        val provinceCode: String, // "03"
        val code: String,
        val normLabel: String   // label pre-normalizada (sin tildes, minúsculas)
    )

    private var cacheUi: List<MunicipioUi>? = null

    /** Carga y prepara todo una vez (con normLabel precalculado). */
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

    /** Búsqueda rápida: sin normalizar cada elemento ni ordenar toda la lista. */
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

    // -------- helpers --------

    private fun prettyName(raw: String): String {
        val m = Regex("^(.+),\\s*(el|la|los|las|l')$", RegexOption.IGNORE_CASE).find(raw.trim())
        return if (m != null) {
            m.groupValues[2].replaceFirstChar { it.titlecase() } + " " + m.groupValues[1]
        } else raw.trim()
    }

    private fun norm(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()

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
