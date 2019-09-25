plugins {
    java
    id("com.github.ben-manes.versions") version ("0.18.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    //we have a compile dependency on the common project
    compileOnly(project(":common"))

    compileOnly("com.inductiveautomation.ignitionsdk:gateway-api:8.0.3")
    toModl("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
}
