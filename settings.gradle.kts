rootProject.name = "graphShow"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val projectProperties = java.util.Properties()
file("gradle.properties").inputStream().use {
    projectProperties.load(it)
}

val versions: String by projectProperties
val koneVersion: String by projectProperties

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
    
    versionCatalogs {
        create("versions").from("dev.lounres:versions:$versions")
        create("kone").from("dev.lounres:kone.versionCatalog:$koneVersion")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.4.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

stal {
    structure {
        "common"("library", "desktop", "web", "compose multiplatform")
        "web"("web", "compose multiplatform")
        "desktop"("desktop", "compose multiplatform")
    }
    tag {
        "kotlin" since { hasAnyOf("desktop", "web", "library") }
        "kotlin jvm target" since { hasAnyOf("desktop") }
        "kotlin js target" since { hasAnyOf("web") }
        "kotlin wasm-js target" since { hasAnyOf("web") }
    }
}