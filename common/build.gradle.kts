
plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    compileOnly("com.inductiveautomation.ignitionsdk:ignition-common:8.0.3")
    compileOnly("com.inductiveautomation.ignitionsdk:perspective-common:8.0.3")
}
