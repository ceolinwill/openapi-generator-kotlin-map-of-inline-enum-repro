# Kotlin client: a map of an inline enum is typed as the enum itself

With the `kotlin` generator, an object property whose `additionalProperties` is an inline enum is generated as the enum type instead of a map of it. For `lessonStatus: { additionalProperties: { type: string, enum: [...] } }` the model gets `val lessonStatus: Today.LessonStatus` instead of `Map<String, Today.LessonStatus>`, so decoding any response with that field fails.

## Reproduction

Requires JDK 17 or newer. The spec is [`openapi.yaml`](openapi.yaml): one object with one map-of-enum property. The build generates the client with the OpenAPI Generator Gradle plugin 7.26.0 (`kotlin`, `jvm-retrofit2`, `kotlinx_serialization`) and runs [`Main.kt`](src/main/kotlin/repro/app/Main.kt), which decodes `{"lessonStatus":{"lesson-1":"ready","lesson-2":"generating"}}`.

```sh
./gradlew -q run
```

Output (stack trace trimmed):

```text
Exception in thread "main" kotlinx.serialization.json.JsonDecodingException: Unexpected JSON token at offset 16: Expected beginning of the string, but got { at path: $.lessonStatus
	at repro.models.Today$$serializer.deserialize(Today.kt:36)
	at repro.app.MainKt.main(Main.kt:10)
```

The generated model, `build/generated/src/main/kotlin/repro/models/Today.kt`:

```kotlin
data class Today (
    @SerialName(value = "lessonStatus")
    val lessonStatus: Today.LessonStatus
) {
    @Serializable
    enum class LessonStatus(val value: kotlin.String) {
        @SerialName(value = "ready") ready("ready"),
        @SerialName(value = "generating") generating("generating"),
        @SerialName(value = "failed") failed("failed");
    }
}
```

## Expected

`val lessonStatus: kotlin.collections.Map<kotlin.String, Today.LessonStatus>`, and the program prints `Today(lessonStatus={lesson-1=ready, lesson-2=generating})`.

## Actual

`DefaultCodegen.updatePropertyForMap` sets `isEnum = true` on a map whose values are an enum, as `updatePropertyForArray` does for arrays. `kotlin-client/data_class_req_var.mustache` and `data_class_opt_var.mustache` handle the array case (`{{#isArray}}…<{{#items.isEnum}}{{classname}}.{{{nameInPascalCase}}}{{/items.isEnum}}>`), but for anything else that `isEnum` they write `{{#isEnum}}{{^isArray}}{{classname}}.{{{nameInPascalCase}}}{{/isArray}}{{/isEnum}}`, which drops the map.

The same happens with every Kotlin client library and serialization library I tried (`jvm-okhttp4` with moshi, gson, jackson and kotlinx_serialization; `jvm-ktor`; `jvm-retrofit2` with moshi), and with `kotlin-spring`. The `java` generator keeps the map.

Naming the value schema as a component (`additionalProperties: { $ref: "#/components/schemas/LessonStatus" }`) avoids it: the property becomes `kotlin.collections.Map<kotlin.String, LessonStatus>`.

## Environment

- OpenAPI Generator 7.26.0 (the latest release; the templates are unchanged on master)
- Kotlin 2.4.21, kotlinx.serialization 1.12.0, Gradle 9.8.2, JDK 25
- macOS 27
