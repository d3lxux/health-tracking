plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "hcmute.edu.vn.healthtracking"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.healthtracking"
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
}

dependencies {
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.applandeo:material-calendar-view:1.9.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    // For efficiency media loading
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    // Media playback
    implementation(libs.exoplayer)
    // Add the dependency for the Firebase AI Logic library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.ai)
    // Required for one-shot operations (to use `ListenableFuture` from Guava Android)
    implementation(libs.guava)
    // Required for streaming operations (to use `Publisher` from Reactive Streams)
    implementation(libs.reactive.streams)
    // For core Markdown rendering
    implementation(libs.core)
}