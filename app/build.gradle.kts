plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.application.storyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.application.storyapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "App Story"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //    navigation component
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")

    implementation ("androidx.datastore:datastore-preferences:1.1.7")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.airbnb.android:lottie:6.3.0")
    implementation( "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-beta01")

    implementation ("androidx.paging:paging-runtime-ktx:3.3.6")

    implementation ("com.google.android.gms:play-services-maps:19.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    // testing
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:4.5.1")
    testImplementation ("org.mockito:mockito-inline:3.12.4")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation( "androidx.arch.core:core-testing:2.2.0")

// Untuk unit test biasa
    testImplementation ("io.mockk:mockk:1.13.5")

// Untuk test coroutine
    // Untuk log SLF4J selama unit test
    testImplementation ("org.slf4j:slf4j-simple:2.0.9")
    testImplementation(kotlin("test"))


}