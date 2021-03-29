plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlin_version"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.4")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.3.4")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")

    implementation("com.github.bumptech.glide:glide:4.11.0")
    kapt("com.github.bumptech.glide:compiler:4.11.0")

    implementation("com.github.pedroSG94.vlc-example-streamplayer:pedrovlc:2.5.14v3")
    implementation("com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:1.9.9")

    implementation("io.ktor:ktor-client-cio:1.5.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("io.ktor:ktor-websockets:1.5.2")
    implementation("io.ktor:ktor-server-netty:1.5.2")

    implementation("com.google.guava:guava:30.1.1-android")
}

android {
    dexOptions {
        javaMaxHeapSize = "3g"
    }
    packagingOptions {
        pickFirst("META-INF/io.netty.versions.properties")
        pickFirst("META-INF/INDEX.LIST")
    }
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.uclan.remotecamera.androidApp"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
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