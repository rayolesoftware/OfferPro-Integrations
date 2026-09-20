plugins {
    id("com.android.library")
    `maven-publish`
}

android {
    namespace = "com.rayole.offerpro.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { /* optional */ }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()      // <— this creates OfferProSdk-vX.Y.Z-sources.jar
            // withJavadocJar()   // optional; requires Dokka/javadoc setup if you want it
        }
    }
}

dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate { from(components["release"]) }
            groupId = "io.offerpro"
            artifactId = "offerpro-sdk"
            version = "2.0.1"
        }
    }
    repositories { maven { name = "distribution"; url = uri(rootProject.file("../dist/maven")) } }
}
