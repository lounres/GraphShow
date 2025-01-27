plugins {
    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
    jvm { withJava() }
    
    sourceSets {
        commonMain {
            dependencies {
                // Compose
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.material3)
                api(compose.components.resources)
                
                // Decompose & Essenty
                api(versions.decompose)
                api(versions.decompose.extensions.compose.multiplatform)
                api(versions.essenty.lifecycle.coroutines)
                
                // Kone
                api(kone.graphs)
                api(kone.computationalGeometry)
                api(kone.misc.composeCanvas)
                
                // kotlinx-serialization
                api(versions.kotlinx.serialization.core)
                api(versions.kotlinx.serialization.json)
                
                // kotlinx-datetime
                implementation(versions.kotlinx.datetime)
                
                // FileKit
                api(libs.fileKit.core)
                api(libs.fileKit.compose)
            }
        }
    }
}