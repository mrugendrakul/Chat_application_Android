import org.gradle.kotlin.dsl.composeCompiler

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}



android {
    signingConfigs {
        create("release") {
            storeFile = file("/home/mrugendra/Documents/keystores/chatApplication.jks")
            storePassword = "goldy777"
            keyAlias = "Key0"
            keyPassword = "goldy777"
        }
    }
    namespace = "com.mad.softwares.chatApplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mad.softwares.chatApplication"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.10.16-beta.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
//        debug {
//        buildConfigField("String", "FCM_BASE_URL", "\"https://fcm.googleapis.com/\"")
//        buildConfigField("String", "FCM_SERVER_KEY", "\"xxxxxxxxxxxxxxxxxxxxxxxx\"")
//        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            multiDexEnabled = true
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.volley)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    // FirebaseUI (for authentication)
    implementation(libs.firebase.ui.auth)
    implementation(libs.androidx.multidex)
    implementation (libs.retrofit)
    implementation (libs.converter.moshi)
    implementation (libs.converter.gson)
    implementation (libs.moshi)
    implementation(libs.accompanist.permissions)
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation (libs.accompanist.pager)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.room.runtime)
//    implementation("com.google.auth:google-auth-library-oauth2-http:1.25.0")
//    implementation(libs.room.ktx)
//    implementation(libs.room.runtime)


    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.lifecycle.viewmodel.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20240600))
    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling)

    debugImplementation(libs.androidx.ui.test.manifest)
}