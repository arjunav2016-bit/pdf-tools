plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.pdftools"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.pdftools"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
        excludes += "/META-INF/DEPENDENCIES"
        excludes += "/META-INF/LICENSE"
        excludes += "/META-INF/LICENSE.txt"
        excludes += "/META-INF/license.txt"
        excludes += "/META-INF/NOTICE"
        excludes += "/META-INF/NOTICE.txt"
        excludes += "/META-INF/notice.txt"
        excludes += "/META-INF/*.kotlin_module"
        excludes += "/META-INF/versions/**"
      }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  implementation(project(":print-helper"))
  val composeBom = platform(libs.androidx.compose.bom)


  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.datastore.preferences)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowsizeclass)
  implementation("androidx.compose.material:material-icons-extended")
  implementation("com.tom-roush:pdfbox-android:2.0.27.0")

  // Apache POI for Office document conversion (Word, PowerPoint, Excel)
  implementation("org.apache.poi:poi-ooxml:5.2.5") {
    exclude(group = "org.apache.xmlgraphics")
    exclude(group = "xml-apis")
  }
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.mockito:mockito-core:5.11.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
  testImplementation("org.robolectric:robolectric:4.12.1")
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.androidx.test.ext.junit)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // ML Kit Text Recognition (On-device OCR)
  implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

  // ML Kit Document Scanner (Camera-based document scanning with edge detection)
  implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Hilt DI
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  // Room Database
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
}
