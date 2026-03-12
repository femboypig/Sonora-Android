import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun String.escapeForBuildConfig(): String {
    return this.replace("\\", "\\\\").replace("\"", "\\\"")
}

val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    if (versionPropertiesFile.exists()) {
        versionPropertiesFile.inputStream().use { load(it) }
    }
}

fun versionPropertyInt(name: String, defaultValue: Int): Int {
    return versionProperties.getProperty(name)?.toIntOrNull() ?: defaultValue
}

val requestedTaskNames = gradle.startParameter.taskNames.map { it.lowercase() }
val shouldAutoBumpVersion = requestedTaskNames.any {
    it.contains("assemble") || it.contains("bundle") || it.contains("install")
}

var autoVersionMajor = versionPropertyInt("VERSION_MAJOR", 2)
var autoVersionMinor = versionPropertyInt("VERSION_MINOR", 1)
var autoVersionPatch = versionPropertyInt("VERSION_PATCH", 0)
var autoVersionCode = versionPropertyInt("VERSION_CODE", 210)

if (shouldAutoBumpVersion) {
    autoVersionCode += 1
    autoVersionPatch += 1
    versionProperties["VERSION_MAJOR"] = autoVersionMajor.toString()
    versionProperties["VERSION_MINOR"] = autoVersionMinor.toString()
    versionProperties["VERSION_PATCH"] = autoVersionPatch.toString()
    versionProperties["VERSION_CODE"] = autoVersionCode.toString()
    versionPropertiesFile.parentFile?.mkdirs()
    versionPropertiesFile.outputStream().use {
        versionProperties.store(it, "Auto-updated by Gradle build")
    }
}

android {
    namespace = "ru.hippo.Sonora"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.hippo.Sonora"
        minSdk = 29
        targetSdk = 36
        versionCode = autoVersionCode
        versionName = "$autoVersionMajor.$autoVersionMinor.$autoVersionPatch"

        val backendBaseUrl = ((project.findProperty("BACKEND_BASE_URL") as String?) ?: "https://api.corebrew.ru").escapeForBuildConfig()
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.media3.exoplayer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
