plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.kushwahahardware"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kushwahahardware"
        minSdk = 26
        targetSdk = 34
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
        compose = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    
    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    debugImplementation(libs.compose.ui.tooling)
    
    // Coil
    implementation(libs.coil.compose)
    
    // MPAndroidChart
    implementation(libs.mpandroidchart)
    
    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    
    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Navigation (XML support if needed, but primarily Compose now)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    
    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    
    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    
    // Biometric
    implementation(libs.biometric)
    
    // PDF Generation
    implementation(libs.itext.kernel)
    implementation(libs.itext.io)
    implementation(libs.itext.layout)
    implementation(libs.zxing.core)

    // CameraX & Barcode Scanning
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Excel Export
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    
    // Email Support
    implementation(libs.jakarta.mail)
    
    // Firebase Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
