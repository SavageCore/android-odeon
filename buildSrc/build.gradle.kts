plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:_")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    implementation("com.google.dagger:hilt-android-gradle-plugin:_")
}
