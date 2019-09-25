plugins {
    java
    id("com.github.ben-manes.versions") version ("0.18.0")
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("com.inductiveautomation.ignitionsdk:designer-api:8.0.3")
}


