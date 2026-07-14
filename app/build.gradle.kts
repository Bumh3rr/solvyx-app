plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "com.solvyx"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.solvyx"
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
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.config)

    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Lifecycle dependencies
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v280)

    // Accompanist Pager
    implementation(libs.accompanist.pager)

    // Lottie Compose
    implementation(libs.lottie.compose)

    // BlurView
    implementation(libs.haze)
    implementation(libs.ui)

    // Preferencias DataStore
    implementation(libs.datastore.preferences)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Material3 dependencies
    implementation(libs.androidx.material3)

    // Runtime saveable dependencies
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.runtime.livedata)

    // Navigation dependencies
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)

    // Gson para la serialización/deserialización de JSON
    implementation(libs.gson)

    // OkHttp para realizar solicitudes HTTP
    implementation(libs.okhttp)

    // HILT - VERSIONES COMPATIBLES CON KOTLIN 2.0.21
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    // KSP para Hilt (en lugar de KAPT)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}