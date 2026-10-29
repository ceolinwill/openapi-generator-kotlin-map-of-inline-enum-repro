plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    id("org.openapi.generator") version "7.26.0"
    application
}

openApiGenerate {
    generatorName = "kotlin"
    library = "jvm-retrofit2"
    inputSpec = layout.projectDirectory.file("openapi.yaml")
    outputDir = layout.buildDirectory.dir("generated")
    packageName = "repro"
    cleanupOutput = true
    configOptions = mapOf("serializationLibrary" to "kotlinx_serialization")
}

sourceSets.main {
    kotlin.srcDir(tasks.openApiGenerate.map { it.outputDir.dir("src/main/kotlin") })
}

application {
    mainClass = "repro.app.MainKt"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.1")
}
