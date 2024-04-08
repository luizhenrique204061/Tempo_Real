package com.luiz.previsodotempo.servicos

import com.luiz.previsodotempo.modelo.Main
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}
//https://api.openweathermap.org/data/2.5/weather?lat={latitude}&lon={longitude}&appid={API key}
//7aa473b67a3b8ca1b1162d87a9bff86d
interface Api {

    @GET("weather")
    fun tempoMap(
        @Query("q") nomeCidade: String,
        @Query("appid") apiKey: String
    ): Call<Main>


    @GET("weather")
    fun tempoMapComCoordenadas(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Call<Main>

    @GET("forecast")
    fun previsao5Dias(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
    ): Call<Main>
}