

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)


}

android {
    namespace = "com.example.yourroom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.yourroom"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compiler)
    implementation(libs.guava)

    androidTestImplementation(libs.androidx.junit) {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }

    // Jetpack Compose
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.compose)

    // DataStore para persistencia del login
    implementation(libs.androidx.datastore.preferences)

// Coil para cargar imágenes (opcional si usas logo como recurso)
    implementation(libs.coil.compose)
    //Lottie
    implementation(libs.lottie.compose)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.material)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // Opcional: Para el manejo de logging
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler) // Asegúrate de tener 'kapt' aplicado
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.logging.interceptor.v4120)

    implementation(libs.material.icons.extended)

    // ➕ Firebase BOM (sin versión en artefactos Firebase)
    implementation(platform(libs.firebase.bom))

    // ➕ Firebase Storage y Auth
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth.ktx)

    // ➕ Coroutines para Tasks (await())
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.compose.ui.text)











    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}