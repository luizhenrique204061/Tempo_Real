package com.luiz.previsodotempo.modelo

import com.google.gson.JsonObject

class Main(
    val main: JsonObject,
    val weather: List<Weather>,
    val name: String,
    val sys: JsonObject
)