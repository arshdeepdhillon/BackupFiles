plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.annotation)
}

android {
    namespace = "com.ad.backupfiles"
    compileSdk = 33

    lint {
        baseline = file("lint-baseline.xml")
        warningsAsErrors = false
    }
    defaultConfig {
        applicationId = "com.ad.backupfiles"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // For updating the version see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.composeBom)
    implementation(composeBom)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.activityCompose)
    implementation(libs.androidx.composeUi)
    implementation(libs.androidx.composeUiGraphics)
    implementation(libs.androidx.composeUiPreview)
    implementation(libs.androidx.material3)
    // ViewModel
    implementation(libs.androidx.lifecycleViewmodelKtx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycleViewmodelCompose)
    // LiveData
    implementation(libs.androidx.lifecycleLivedataKtx)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycleRuntimeKtx)
    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycleRuntimeCompose)
    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycleViewmodelSavedstate)
    implementation(libs.androidx.navigationCompose)
    implementation(libs.smbj)
    implementation(libs.androidx.roomRuntime)
    annotationProcessor(libs.androidx.roomCompiler)
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.roomCompiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.roomKtx)
    implementation(libs.androidx.documentfile)
    // WorkManger
    implementation(libs.androidx.workRuntimeKtx)

    // For test
    androidTestImplementation(composeBom)
    implementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.composeUiJunit4)
    androidTestImplementation(libs.androidx.espressoCore)
    androidTestImplementation(libs.cashturbine)
    debugImplementation(libs.androidx.composeUiTooling)
    debugImplementation(libs.androidx.composeUiTestManifest)

    // Unit tests
    testImplementation(libs.kotlin.coroutineTest)
    testImplementation(libs.cashturbine)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.mockkBundle)
}