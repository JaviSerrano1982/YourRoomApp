# YourRoom – Frontend Android (Jetpack Compose)

Aplicación Android para **YourRoom**, una plataforma donde entrenadores personales pueden **registrarse y alquilar salas/gimnasios por horas** para entrenamientos.  
Este frontend consume la API del backend (Spring Boot + MySQL + JWT) y está construido con **Kotlin**, **Jetpack Compose**, **MVVM** y **Retrofit**.

> 📸 **Screenshots**  
> _(añade tus imágenes aquí cuando quieras)_
>
> | Inicio | Login | Perfil | Mis salas |
> |---|---|---|---|
> | ![s1](docs/images/s1.png) | ![s2](docs/images/s2.png) | ![s3](docs/images/s3.png) | ![s4](docs/images/s4.png) |

---

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Características](#características)
- [Requisitos previos](#requisitos-previos)
- [Configuración del proyecto](#configuración-del-proyecto)
- [Compilación y ejecución](#compilación-y-ejecución)
- [Estructura de paquetes](#estructura-de-paquetes)
- [Gestión de dependencias (Version Catalogs)](#gestión-de-dependencias-version-catalogs)
- [Variables y entornos](#variables-y-entornos)
- [Buenas prácticas y calidad](#buenas-prácticas-y-calidad)
- [Resolución de problemas](#resolución-de-problemas)
- [Roadmap](#roadmap)
- [Licencia](#licencia)
- [Autor](#autor)

---

## Arquitectura

- **MVVM + Clean-ish**: UI (Compose) → ViewModel → UseCases/Repos → Data Source (Retrofit).
- **State hoisting** y **unidireccional** para el estado UI.
- **Navegación** con `Navigation-Compose`.
- **Persistencia ligera** para sesión/token (DataStore o SharedPreferences).
- **Módulos** (si aplica): `app` (presentación) y paquetes por feature.

---

## Tecnologías

- **Kotlin**, **Kotlin Coroutines/Flows**
- **Jetpack Compose** (Material 3)
- **Navigation-Compose**
- **Retrofit + OkHttp** (API REST)
- **Gson/Moshi** (JSON)
- **Coil** (carga de imágenes)
- **DataStore** (token/ajustes)
- **Hilt** (inyección de dependencias) _(opcional, si lo usas)_
- **JUnit / MockK / Turbine** (tests) _(si aplica)_

---

## Características

- **Onboarding** con fondo degradado y **slider de progreso**.
- **Login/Registro** con validación y consumo de API (JWT).
- **Sesión persistente**: reconoce al usuario tras abrir la app.
- **Perfil de usuario**: ver/editar datos y **subir imagen** (se envía al backend).
- **Listado “Mis Salas”**: crear/editar/borrar salas (CRUD contra API).  
- **Estados de UI**: loading, éxito, error con mensajes claros.
- **Soporte para distintos entornos** (dev / prod) vía `BuildConfig`.

> Nota: La disponibilidad exacta de features depende de la rama/versión del proyecto.

---

## Requisitos previos

- **Android Studio** Ladybug Feature Drop (2024.2.2) o superior.
- **JDK 17**.
- **Gradle** wrapper incluido en el proyecto.
- Backend de **YourRoom** en ejecución y accesible (URL base).

---

## Configuración del proyecto

1. **Clona el repo**:
   ```bash
   git clone https://github.com/tu-usuario/yourroom-android.git
   cd yourroom-android
   ```

2. **Configura la URL del backend** (baseUrl):
   - Opción A – por **BuildConfig** (recomendado):
     - En `app/build.gradle.kts`, dentro de cada `buildType`:
       ```kotlin
       buildTypes {
           debug {
               buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
           }
           release {
               buildConfigField("String", "API_BASE_URL", "\"https://api.tudominio.com/\"")
               isMinifyEnabled = true
               proguardFiles(
                   getDefaultProguardFile("proguard-android-optimize.txt"),
                   "proguard-rules.pro"
               )
           }
       }
       ```
     - En tu cliente de Retrofit:
       ```kotlin
       private const val BASE_URL = BuildConfig.API_BASE_URL
       ```
   - Opción B – por **resources**:
     - Añade `res/xml/network_security_config.xml` si necesitas permitir HTTP en debug (emulador).
     - Añade `res/values/config.xml` con `<string name="api_base_url">...</string>` y léelo desde código.

3. **Permisos/Network Security (solo si usas HTTP en local)**  
   Emulador (Android) para localhost del host: `http://10.0.2.2:8080/`.  
   Si pruebas en **dispositivo físico**, usa la IP de tu máquina.

4. **Keystore (release)**  
   Si vas a generar APK/AAB de release, configura tu keystore **fuera** del repo y usa variables locales.

---

## Compilación y ejecución

- **Debug en emulador**:
  - Backend local en el host: `http://10.0.2.2:8080/`
  - Pulsa ▶ en Android Studio sobre el módulo `app`.

- **Desde terminal**:
  ```bash
  ./gradlew clean assembleDebug
  ./gradlew installDebug
  ```

- **Release**:
  ```bash
  ./gradlew bundleRelease
  ```
  El AAB se genera en `app/build/outputs/bundle/release/`.

---

## Estructura de paquetes

```
com.yourroom/
├─ ui/                  # Pantallas Compose y componentes
│  ├─ theme/            # Colores, tipografía, shapes
│  ├─ nav/              # Gráfico de navegación
│  └─ screens/
│     ├─ onboarding/
│     ├─ auth/          # Login/Register
│     ├─ profile/
│     └─ rooms/         # Mis salas (listado/edición)
├─ data/
│  ├─ remote/           # DTOs, Retrofit services
│  ├─ repository/
│  └─ local/            # DataStore / cache
├─ domain/              # Modelos y use cases (si aplica)
├─ di/                  # Módulos Hilt (si usas Hilt)
└─ utils/               # Helpers, Result wrappers, etc.
```

---

## Gestión de dependencias (Version Catalogs)

Si usas `libs.versions.toml`:

**`gradle/libs.versions.toml` (ejemplo mínimo)**
```toml
[versions]
kotlin = "1.9.24"
agp = "8.5.2"
compose = "1.7.4"
material3 = "1.3.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
coil = "2.6.0"
hilt = "2.51.1"
navigation = "2.8.3"

[libraries]
compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "compose" }
compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "material3" }
compose-activity = "androidx.activity:activity-compose:1.9.3"
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }

coil = { module = "io.coil-kt:coil-compose", version.ref = "coil" }

hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
```

**`app/build.gradle.kts` (uso)**
```kotlin
dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.coil)

    // Hilt (si aplica)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

---

## Variables y entornos

- **JWT**: se almacena de forma segura en **DataStore** o similar.
- **BASE_URL**: cambia por `buildType` (debug/prod) mediante `BuildConfig`.
- **No subas secretos** al repo. Añade a `.gitignore`:
  ```
  *.keystore
  *.jks
  local.properties
  your_keystore_passwords.txt
  ```
- Si incluyes ejemplos, usa **placeholders** (p. ej. `http://10.0.2.2:8080/`).

---

## Buenas prácticas y calidad

- **Formateo**: Ktlint/Spotless (opcional).
- **Tests**:  
  - Unit tests para ViewModels y UseCases.  
  - Fakes/Mocks para repos/servicios.
- **Errores/Estados**: usar un `sealed class Result` (`Loading/Success/Error`).
- **Accesibilidad**: contentDescription en imágenes y tamaños adaptativos.
- **Rendimiento**: evitar recomposiciones innecesarias (usar `remember`, `derivedStateOf`).

---

## Resolución de problemas

- **HTTP en emulador**: usa `10.0.2.2` como host.  
- **CORS/Policies**: el frontend Android no tiene CORS, pero si el backend está detrás de proxy/HTTPS, revisa certificados para dispositivos físicos.
- **Timeouts**: sube `read/connect timeout` en OkHttp durante desarrollo si el backend está en local.
- **Imagen de perfil**: asegúrate de enviar el **multipart** correcto al endpoint del backend.

---

## Roadmap

- [ ] Validaciones accesibles y mensajes localizados (i18n).
- [ ] Estado offline/caché básica.
- [ ] Tests de UI (Compose UI Test).
- [ ] Modo oscuro fino y temas dinámicos.
- [ ] Listado/booking de salas públicas y buscador por mapa.
- [ ] Notificaciones (recordatorios de reservas).

---

## Licencia

Proyecto de uso **académico/demostrativo**. Todos los derechos reservados por el autor.

---

## Autor

**Javier Serrano**  
Desarrollador de apps multiplataforma  
[GitHub](https://github.com/tu-usuario) · [LinkedIn](https://linkedin.com/in/javier-serrano-jiménez-aaba69117)
