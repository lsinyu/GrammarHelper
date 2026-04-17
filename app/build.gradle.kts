import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.grammarhelper"
    compileSdk = 36
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.grammarhelper"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Fetch GEMINI_API_KEY from local.properties OR System Environment (for CI/CD flexibility)
        val properties = Properties()
        val propertiesFile = project.rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }
        
        // Priority: local.properties > Environment Variable > Placeholder
        val apiKey = properties.getProperty("GEMINI_API_KEY") 
            ?: System.getenv("GEMINI_API_KEY") 
            ?: "YOUR_GEMINI_API_KEY_NOT_SET"
            
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
        
        if (apiKey == "YOUR_GEMINI_API_KEY_NOT_SET") {
            println("⚠️ WARNING: GEMINI_API_KEY is not set in local.properties or Environment Variables. Grammar features will not work.")
        }
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // AI & Network
    implementation(libs.okhttp)
    implementation(libs.gson)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Charts
    implementation(libs.mpandroidchart)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Google Auth
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
