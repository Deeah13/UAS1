plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.sipakjabat"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.sipakjabat"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)

    // Dependensi wajib untuk ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Karena layout sebelumnya menggunakan Material Components, pastikan ini juga ada
    implementation("com.google.android.material:material:1.11.0")

    // Retrofit untuk koneksi ke API Backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter GSON untuk mengubah JSON menjadi Object Kotlin secara otomatis
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Logging Interceptor untuk memantau trafik data di Logcat (opsional tapi disarankan)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}