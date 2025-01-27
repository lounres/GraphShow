//import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.LibrariesForVersions
import org.gradle.accessors.dm.RootProjectAccessor
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import kotlin.collections.plus

plugins {
    alias(versions.plugins.kotlin.multiplatform) apply false
    alias(versions.plugins.kotlinx.serialization) apply false
    alias(versions.plugins.kotlin.compose) apply false
    alias(versions.plugins.compose.multiplatform) apply false
}

val jvmTargetVersion: String by project

val Project.versions: LibrariesForVersions get() = rootProject.extensions.getByName<LibrariesForVersions>("versions")
//val Project.libs: LibrariesForLibs get() = rootProject.extensions.getByName<LibrariesForLibs>("libs")
val Project.projects: RootProjectAccessor get() = rootProject.extensions.getByName<RootProjectAccessor>("projects")
fun PluginAware.apply(pluginDependency: PluginDependency) = apply(plugin = pluginDependency.pluginId)
fun PluginAware.apply(pluginDependency: Provider<PluginDependency>) = apply(plugin = pluginDependency.get().pluginId)
fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

group = "dev.lounres"
version = "0.0.0"


allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.kotlin.link")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        mavenLocal()
    }
}

stal {
    action {
        "kotlin jvm target" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvmToolchain(jvmTargetVersion.toInt())
                    jvm()
                }
            }
        }
        "kotlin js target" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    js {
                        moduleName = project.path.substring(startIndex = 1).replace(':', '-')
                        browser()
//                        nodejs()
//                        d8()
                    }
                }
            }
        }
        "kotlin wasm-js target" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    @OptIn(ExperimentalWasmDsl::class)
                    wasmJs {
                        moduleName = project.path.substring(startIndex = 1).replace(':', '-')
                        browser()
//                        nodejs()
//                        d8()
                    }
                }
            }
        }
        "kotlin" {
            apply(versions.plugins.kotlin.multiplatform)
            configure<KotlinMultiplatformExtension> {
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                compilerOptions {
                    freeCompilerArgs = freeCompilerArgs.get() + listOf(
                        "-Xklib-duplicated-unique-name-strategy=allow-all-with-warning",
//                        "-Xklib-duplicated-unique-name-strategy=allow-first-with-warning",
                        "-Xexpect-actual-classes",
                        "-Xconsistent-data-class-copy-visibility",
                    )
                }
                sourceSets {
                    all {
                        languageSettings {
                            progressiveMode = true
                            enableLanguageFeature("ContextParameters")
                            enableLanguageFeature("ValueClasses")
                            enableLanguageFeature("ContractSyntaxV2")
                            enableLanguageFeature("ExplicitBackingFields")
                            optIn("kotlin.contracts.ExperimentalContracts")
                            optIn("kotlin.ExperimentalStdlibApi")
                            optIn("kotlin.ExperimentalUnsignedTypes")
                            optIn("kotlin.uuid.ExperimentalUuidApi")
                            optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                        }
                    }
                    commonTest {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
            afterEvaluate {
                the<KotlinMultiplatformExtension>().targets.withType<KotlinJvmTarget> {
                    @OptIn(ExperimentalKotlinGradlePluginApi::class)
                    compilerOptions {
                        jvmTarget = JvmTarget.fromTarget(jvmTargetVersion)
                        freeCompilerArgs = freeCompilerArgs.get() + listOf("-Xlambdas=indy")
                    }
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
            pluginManager.withPlugin("org.gradle.java") {
                configure<JavaPluginExtension> {
                    sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
                    targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
                }
                tasks.withType<Test> {
                    useJUnitPlatform()
                }
            }
        }
        "library" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    explicitApi = ExplicitApiMode.Warning
                }
            }
        }
        "compose multiplatform" {
            apply(versions.plugins.kotlin.compose)
            apply(versions.plugins.compose.multiplatform)
        }
    }
}