// ============ ПЛАГИНЫ ============
plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

// ============ ГРУППА И ВЕРСИЯ ============
group = "com.example"
version = "1.0.0"

// ============ РЕПОЗИТОРИИ ============
repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

// ============ ЗАВИСИМОСТИ ============
dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.animation)
    implementation(compose.components.resources)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation(compose.components.resources)

}

// ============ НАСТРОЙКА СБОРКИ ============
compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Xmx2G")

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "ScoreKeeper"
            packageVersion = "1.0.0"
            windows {
                menu = true
                shortcut = true
            }
        }
    }
}

// ============ JAVA ============
kotlin {
    jvmToolchain(17)
}