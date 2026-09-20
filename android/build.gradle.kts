plugins {
    id("com.android.application") version "8.6.1" apply false
    id("com.android.library")    version "8.6.1" apply false
    kotlin("android")            version "2.0.20" apply false
}

allprojects {
    group = "com.github.rayolesoftware" // for JitPack
    version = "0.0.0-SNAPSHOT"           // JitPack uses your git tag version
}

tasks.register("syncDistribution") {
    dependsOn(":offerpro-sdk:publishReleasePublicationToDistributionRepository")
    doLast {
        listOf("flutter", "react-native").forEach { framework ->
            copy { from("../dist/maven"); into("../$framework/android/maven") }
            copy {
                from("offerpro_sdk/build/outputs/aar/offerpro-sdk-release.aar")
                into("../$framework/android/libs")
            }
        }
        copy { from("offerpro_sdk/build/outputs/aar/offerpro-sdk-release.aar"); into("../dist") }
    }
}
