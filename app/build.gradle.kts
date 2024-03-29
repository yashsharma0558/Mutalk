import com.android.build.gradle.internal.testing.screenshot.PROPERTIES
import org.gradle.api.internal.properties.GradleProperties
import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services")
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()

localPropertiesFile.inputStream().use { input ->
    localProperties.load(input)
}
android {
    android.buildFeatures.buildConfig =  true
    namespace = "com.example.mutalk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mutalk"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "ZEGO_APP_SIGN", localProperties.getProperty("ZEGO_APP_SIGN"))
        buildConfigField("String", "ZEGO_APP_ID", localProperties.getProperty("ZEGO_APP_ID"))

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.camera.core)
    implementation("im.zego:express-video:3.12.4")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.34")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:latest.release")
    implementation(libs.firebase.auth)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    androidTestImplementation("org.testng:testng:6.9.6")

}