buildscript {
    repositories {
        google()
        mavenCentral()
    }

    extra.apply {
        set("composeVersion", "1.1.1")
        set("datastoreVersion", "1.0.0")
        set("hiltVersion", "2.42")
        set("hiltComposeVersion", "1.0.0")
        set("roomVersion", "2.4.2")
        set("coroutinesVersion", "1.6.1")
        set("lifecycleVersion", "2.4.1")
        set("exoPlayerVersion", "1.0.0-alpha03")
        set("accompanistVersion", "0.23.1")
    }

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.42")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.0")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
        classpath(kotlin("serialization", version = "1.6.10"))
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