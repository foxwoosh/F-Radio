buildscript {
    repositories {
        google()
        mavenCentral()
    }

    extra.apply {
        set("compose_version", "1.1.1")
        set("datastore_version", "1.0.0")
        set("hilt_version", "2.38.1")
        set("room_version", "2.4.2")
        set("coroutines_version", "1.6.1")
        set("lifecycle_version", "2.4.1")
        set("exo_player_version", "2.17.1")
    }

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
        classpath(kotlin("serialization", version = "1.6.10"))
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "7.2.0" apply false
    id("com.android.library") version "7.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}