plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.io.realm.kotlin) apply false
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.0" apply false
}