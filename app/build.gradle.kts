

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)



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

    // --- Core Android / Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material)


    // --- Jetpack Compose (BOM + UI) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui.text)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // --- Hilt (KSP) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- DataStore ---
    implementation(libs.androidx.datastore.preferences)

    // --- Networking ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor) // deja solo 1 interceptor

    // --- Media / UI extras ---
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.material.icons.extended)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth.ktx)

    // --- Coroutines (Tasks await) ---
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Guava (si realmente lo usas; si no, quítalo) ---
    implementation(libs.guava)

    // --- Tests ---
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit) {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
