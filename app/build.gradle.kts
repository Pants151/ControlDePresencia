plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.controldepresencia2026"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.controldepresencia2026"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit para conectar con tu Flask en PythonAnywhere
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Arquitectura MVVM para Java (ViewModel y LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // Servicios de Ubicación y Mapas
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Retrofit y conversor GSON
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Almacenamiento seguro del token JWT
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Mapa OSMDroid (OpenStreetMap para Android)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
}