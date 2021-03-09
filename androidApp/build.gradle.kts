plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.guava:guava:30.1-android")
    implementation("com.github.ar-android:libstreaming:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.3.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(files("libs/http-2.2.1.jar"))
    implementation(files("libs/sun-common-server.jar"))

}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.uclan.remotecamera.androidApp"
        minSdkVersion(27)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}