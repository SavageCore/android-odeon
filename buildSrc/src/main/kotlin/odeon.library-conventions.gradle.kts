/*
 * Copyright 2021 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        // Include Android resources in unit tests to be resolved by Robolectric.
        unitTests.isIncludeAndroidResources = true
    }

    buildTypes {
        getByName("release") {
            kotlinOptions {
                // Compile releases without non-null assertions
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xno-param-assertions",
                    "-Xno-call-assertions",
                    "-Xno-receiver-assertions"
                )
            }
        }
    }
}

dependencies {
    // General-purpose Kotlin libraries
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(KotlinX.coroutines.core)

    // General-purpose Android-specific libraries
    implementation(AndroidX.core.ktx)
    implementation(JakeWharton.timber)

    // Hilt - dependency injection
    implementation(Google.dagger.hilt.android)
    kapt(Google.dagger.hilt.compiler)

    // Provides the instance of SharedPreferences
    implementation(AndroidX.preferenceKtx)

    // Unit Test execution environment
    testImplementation(Testing.robolectric)
    testImplementation(AndroidX.test.ext.junitKtx)

    // Unit Testing utilities
    testImplementation(KotlinX.coroutines.test)
    testImplementation(KotlinX.coroutines.debug)
    testImplementation(AndroidX.test.core)

    // Unit Test assertion libraries
    testImplementation(Kotlin.test.junit)
    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.kotest.property)
    testImplementation(Testing.mockK)

    // Instrumentation test environment
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.runner)

    // Instrumentation test assertion libraries
    androidTestImplementation(Kotlin.test.junit)
    androidTestImplementation(AndroidX.test.coreKtx)
    androidTestImplementation(Testing.kotest.assertions.core)
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

hilt {
    enableAggregatingTask = true
}

tasks.withType<Test>().configureEach {
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
    }
}
