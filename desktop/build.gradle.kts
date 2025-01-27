import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.common)
                
                // Compose
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose {
    resources {
        packageOfResClass = "dev.lounres.graphShow.desktop.resources"
        generateResClass = always
        publicResClass = true
    }
    desktop {
        application {
            mainClass = "dev.lounres.graphShow.desktop.MainKt"

            buildTypes.release.proguard {
//            obfuscate = true
            }

            nativeDistributions {
                packageName = "GraphShow"
                packageVersion = version as String
                description = "GraphShow is a graph viewer"
                copyright = "© 2024 Gleb Minaev. All rights reserved."
                vendor = "Gleb Minaev"
//                licenseFile = rootProject.file("LICENSE")


                targetFormats(
                    // Windows
                    TargetFormat.Exe,
                    TargetFormat.Msi,
                    // Linux
//                TargetFormat.Deb,
//                TargetFormat.Rpm,
                    // maxOS
//                TargetFormat.Dmg,
//                TargetFormat.Pkg
                )

                windows {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.ico")
//                    console = true
//                    perUserInstall = true
//                    upgradeUuid = ""
                }

                linux {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.png")
//                    rpmLicenseType = ""
                }

                macOS {
//                    iconFile = project.file("")
                }
            }
        }
    }
}