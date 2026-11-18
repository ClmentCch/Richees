import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.richyss.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.richyss.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    // Release signing is read from the environment (CI secrets) or from
    // ~/.gradle/gradle.properties. Without them the release build is signed with the
    // debug key and the build prints an explicit warning.
    val storeFilePath: String? = System.getenv("RICHYSS_KEYSTORE_PATH")
        ?: providers.gradleProperty("richyss.keystore.path").orNull
    val storePasswordValue: String? = System.getenv("RICHYSS_KEYSTORE_PASSWORD")
        ?: providers.gradleProperty("richyss.keystore.password").orNull
    val keyAliasValue: String? = System.getenv("RICHYSS_KEY_ALIAS")
        ?: providers.gradleProperty("richyss.key.alias").orNull
    val keyPasswordValue: String? = System.getenv("RICHYSS_KEY_PASSWORD")
        ?: providers.gradleProperty("richyss.key.password").orNull

    val hasReleaseKey = storeFilePath != null && storePasswordValue != null &&
        keyAliasValue != null && keyPasswordValue != null

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(storeFilePath!!)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseKey) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "Richyss: no release keystore configured, the release APK is signed " +
                        "with the debug key. Set RICHYSS_KEYSTORE_PATH, " +
                        "RICHYSS_KEYSTORE_PASSWORD, RICHYSS_KEY_ALIAS and " +
                        "RICHYSS_KEY_PASSWORD to produce a durable signature."
                )
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "DebugProbesKt.bin"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
}
