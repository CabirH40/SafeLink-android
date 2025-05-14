@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)

}

android {
    namespace = "com.example.safelink"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.safelink"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}





dependencies {
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation ("org.tensorflow:tensorflow-lite:2.16.1")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4") //أو أحدث إصدار. // أو إصدار أحدث
    implementation ("androidx.browser:browser:1.7.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
