buildscript {
    repositories {
        google()
        mavenCentral()
    }

    extra.apply {
        set("composeVersion", "1.2.0-rc02")
        set("composeCompilerVersion", "1.2.0")
        set("accompanistVersion", "0.24.12-rc")
        set("datastoreVersion", "1.0.0")
        set("hiltVersion", "2.42")
        set("hiltComposeVersion", "1.0.0")
        set("roomVersion", "2.4.2")
        set("coroutinesVersion", "1.6.1")
        set("lifecycleVersion", "2.4.1")
        set("exoPlayerVersion", "1.0.0-alpha03")
    }

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.42")
        classpath("com.google.gms:google-services:4.3.12")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")

        val kotlinVersion = "1.6.21"
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "7.2.1" apply false
    id("com.android.library") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}