plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.karpeko.coffee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.karpeko.coffee"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}



dependencies {

    // Для Firebase BoM (рекомендуется):
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.firebase:firebase-auth:22.1.1") // проверьте актуальную версию
    implementation("com.google.android.gms:play-services-auth:20.6.0") // Google Sign-In

    implementation("androidx.work:work-runtime:2.7.1") // WorkManager
    implementation("com.google.firebase:firebase-firestore:24.4.1")

    // Решение для ListenableFuture (выберите один вариант)

    // Вариант 1: Добавьте Guava (если используете Firebase с Guava)
    implementation("com.google.guava:guava:31.1-android")

    // ИЛИ Вариант 2: Используйте специальную версию для Android
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation("com.airbnb.android:lottie:6.0.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.yandex.android:maps.mobile:4.3.1-full")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.activity)
    implementation(libs.annotation)
    implementation(libs.transportation.consumer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}