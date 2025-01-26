import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

kotlin {
    js {
        moduleName = "GraphShow"
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-debugger-custom-formatters")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "GraphShow.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDir.path)
                        add(rootDir.path + "/web")
                    }
                }
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "GraphShow"
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-debugger-custom-formatters")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "GraphShow.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDir.path)
                        add(rootDir.path + "/web")
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        jsMain {
            dependencies {
                implementation(projects.common)
                
                // Compose
                
            }
        }
        wasmJsMain {
            dependencies {
                implementation(projects.common)
                
                // Compose
                
            }
        }
    }
}

compose {
    resources {
        packageOfResClass = "dev.lounres.graphShow.web.resources"
        generateResClass = always
        publicResClass = true
    }
    web
}