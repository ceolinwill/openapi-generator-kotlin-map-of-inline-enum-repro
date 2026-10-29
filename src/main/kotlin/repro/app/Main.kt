package repro.app

import kotlinx.serialization.json.Json
import repro.models.Today

fun main() {
    println(Json.decodeFromString<Today>("""{"lessonStatus":{"lesson-1":"ready","lesson-2":"generating"}}"""))
}
