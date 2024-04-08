package com.luiz.previsodotempo
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.luiz.previsodotempo.constantes.Const
import com.luiz.previsodotempo.databinding.ActivityTelaPrincipalBinding
import com.luiz.previsodotempo.modelo.Main
import com.luiz.previsodotempo.servicos.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat

class TelaPrincipal : AppCompatActivity() {
    private lateinit var binding: ActivityTelaPrincipalBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PREFERENCIA = "Minha Preferência"
    private val ESTADO_DO_SWITCH = "Estado do switch"
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTelaPrincipalBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Obter a referência para Shared Preferences
        val preferencia = getSharedPreferences(PREFERENCIA, Context.MODE_PRIVATE)

        val estadoSwitchAoIniciar = preferencia.getBoolean(ESTADO_DO_SWITCH, true)
        binding.trocarTema.isChecked = estadoSwitchAoIniciar

        atualizarTemaAoIniciar(binding.trocarTema.isChecked)


        binding.trocarTema.setOnClickListener {
            val editor = preferencia.edit()
            editor.putBoolean(ESTADO_DO_SWITCH, binding.trocarTema.isChecked)
            editor.apply()
            if (binding.trocarTema.isChecked) {
                binding.layout.setBackgroundColor((Color.parseColor("#000000")))
                binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_escuro)
                binding.textoTituloInformacoes.setTextColor(Color.parseColor("#000000"))
                binding.textoInformacoes1.setTextColor(Color.parseColor("#000000"))
                binding.textoInformacoes2.setTextColor(Color.parseColor("#000000"))
                binding.buscarCidade.setTextColor(Color.parseColor("#000000"))
            } else {
                binding.layout.setBackgroundColor((Color.parseColor("#396BCB")))
                binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_claro)
                binding.textoTituloInformacoes.setTextColor(Color.parseColor("#FFFFFF"))
                binding.textoInformacoes1.setTextColor(Color.parseColor("#FFFFFF"))
                binding.textoInformacoes2.setTextColor(Color.parseColor("#FFFFFF"))
                binding.buscarCidade.setTextColor(Color.parseColor("#000000"))
            }
        }

        binding.botaoBuscar.setOnClickListener {
            val buscarCidade = binding.buscarCidade.text.toString()
            recolherTeclado()

            if (buscarCidade.isEmpty()) {
                getLastLocation()
                
            } else {
                binding.progressbar.visibility = View.VISIBLE

                val retrofit = Retrofit.Builder()

                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .build()
                    .create(Api::class.java)

                retrofit.tempoMap(buscarCidade, Const.API_KEY).enqueue(object : Callback<Main> {
                    override fun onResponse(call: Call<Main>, response: Response<Main>) {
                        if (response.isSuccessful) {
                            respostaServidor(response)
                            binding.progressbar.visibility = View.GONE
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Cidade inválida",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            binding.progressbar.visibility = View.GONE
                        }
                    }

                    override fun onFailure(call: Call<Main>, t: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            "Erro fatal de servidor",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        binding.progressbar.visibility = View.GONE
                    }

                })
            }
        }

        binding.imagemClima.setOnClickListener {

        }
    }

    override fun onStart() {
        super.onStart()
        getLastLocation()

        val preferencia = getSharedPreferences(PREFERENCIA, Context.MODE_PRIVATE)
        val estadoSwitchAoIniciar = preferencia.getBoolean(ESTADO_DO_SWITCH, false)
        binding.trocarTema.isChecked = estadoSwitchAoIniciar
        atualizarTemaAoIniciar(estadoSwitchAoIniciar)
    }

    private fun atualizarTemaAoIniciar(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            // Definir as cores do tema escuro
            binding.layout.setBackgroundColor(Color.parseColor("#000000"))
            binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_escuro)
            binding.textoTituloInformacoes.setTextColor(Color.parseColor("#000000"))
            binding.textoInformacoes1.setTextColor(Color.parseColor("#000000"))
            binding.textoInformacoes2.setTextColor(Color.parseColor("#000000"))
            binding.buscarCidade.setTextColor(Color.parseColor("#000000"))
        } else {
            // Definir as cores do tema claro
            binding.layout.setBackgroundColor(Color.parseColor("#396BCB"))
            binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_claro)
            binding.textoTituloInformacoes.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textoInformacoes1.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textoInformacoes2.setTextColor(Color.parseColor("#FFFFFF"))
            binding.buscarCidade.setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun retrofitWeather(latitude: Double, longitude: Double) {

        binding.progressbar.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(Api::class.java)

        retrofit.tempoMapComCoordenadas(latitude, longitude, Const.API_KEY).enqueue(object : Callback<Main> {
            override fun onResponse(call: Call<Main>, response: Response<Main>) {
                if (response.isSuccessful) {
                    respostaServidor(response)
                    binding.progressbar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Main>, t: Throwable) {
                Toast.makeText(applicationContext, "Cidade inválida", Toast.LENGTH_SHORT)
                    .show()
                binding.progressbar.visibility = View.GONE
            }

        })
    }


    private fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(
                this@TelaPrincipal,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@TelaPrincipal,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@TelaPrincipal,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_LOCATION
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    retrofitWeather(location.latitude, location.longitude)

                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting location", e)
            }
    }


    private fun respostaServidor(response: Response<Main>) {
        val main = response.body()!!.main

        val temp = main.get("temp").toString()
        val tempMin = main.get("temp_min").toString()
        val tempMax = main.get("temp_max").toString()
        val humidity = main.get("humidity").toString()

        val sys = response.body()!!.sys
        val country = sys.get("country").asString
        var pais = ""

        val weather = response.body()!!.weather
        val mainWeather = weather[0].main
        val description = weather[0].description

        val name = response.body()!!.name

        //Converter Kelvin em Graus Celsius - Fórmula: 298 - 273,15

        val temp_c = (temp.toDouble() - 273.15)
        val tempMin_c = (tempMin.toDouble() - 273.15)
        val tempMax_c = (tempMax.toDouble() - 273.15)
        val decimalFormatado = DecimalFormat("00")

        val descricao = when(description) {
            "clear Sky" -> {
                "Céu Limpo"
            }
            "few clouds" -> {
                "Poucas Nuvens"
            }
            "scattered clouds" -> {
                "Nublado"
            }
            "broken clouds" -> {
                "Encoberto"
            }
            "shower rain" -> {
                "Chuva Passageira"
            }
            "rain" -> {
                "Chuva"
            }
            "thunderstorm" -> {
                "Tempestade"
            }
            "snow" -> {
                "Neve"
            }
            "thunderstorm with light rain" -> {
                "Trovoada com\n Chuva Fraca"
            }
            "drizzle" -> {
                "Chuva Fraca"
            }
            "mist" -> {
                "Névoa"
            }
            "haze" -> {
                "Névoa"
            }
            "fog" -> {
                "Nevoeiro"
            }
            "overcast clouds" -> {
                "Nuvens Encobertas"
            }
            "light rain" -> {
                "Chuva Fraca"
            }
            else -> {
                "Céu Limpo"
            }
        }

        if (mainWeather.equals("Clouds") && description.equals("few clouds")) {
            binding.imagemClima.setBackgroundResource(R.drawable.flewclouds)
        } else if (mainWeather.equals("Clouds") && description.equals("scattered clouds")) {
            binding.imagemClima.setBackgroundResource(R.drawable.clouds)
        } else if (mainWeather.equals("Clouds") && description.equals("broken clouds")) {
            binding.imagemClima.setBackgroundResource(R.drawable.brokenclouds)
        } else if (mainWeather.equals("Clouds") && description.equals("overcast clouds")) {
            binding.imagemClima.setBackgroundResource(R.drawable.brokenclouds)
        } else if (mainWeather.equals("Clear") && description.equals("clear sky")) {
            binding.imagemClima.setBackgroundResource(R.drawable.clearsky)
        }
        else if (mainWeather.equals("Snow")) {
            binding.imagemClima.setBackgroundResource(R.drawable.snow)
        } else if (mainWeather.equals("Rain") && description.equals("light rain")) {
            binding.imagemClima.setBackgroundResource(R.drawable.rain)
        } else if (mainWeather.equals("Rain") && description.equals("rain")) {
            binding.imagemClima.setBackgroundResource(R.drawable.rain)
        } else if (mainWeather.equals("Drizzle")) {
            binding.imagemClima.setBackgroundResource(R.drawable.rain)
        } else if (mainWeather.equals("Thunderstorm")) {
            binding.imagemClima.setBackgroundResource(R.drawable.thunderstorm)
        } else if (mainWeather.equals("Mist")) {
            binding.imagemClima.setBackgroundResource(R.drawable.brokenclouds)
        } else if (mainWeather.equals("Haze")) {
            binding.imagemClima.setBackgroundResource(R.drawable.brokenclouds)
        } else if (mainWeather.equals("Fog")) {
            binding.imagemClima.setBackgroundResource(R.drawable.brokenclouds)
        } else if (mainWeather.equals("Clear")) {
            binding.imagemClima.setBackgroundResource(R.drawable.clearsky)
        }


        when {
            country.equals("BR") -> {
                pais = "Brasil"
            }
            country.equals("US") -> {
                pais = "Estados Unidos"
            }
            country.equals("AF") -> {
                pais = "Afeganistão"
            }
            country.equals("AL") -> {
                pais = "Albânia"
            }
            country.equals("DZ") -> {
                pais = "Argélia"
            }
            country.equals("AS") -> {
                pais = "Samoa Americana"
            }
            country.equals("AD") -> {
                pais = "Andorra"
            }
            country.equals("AO") -> {
                pais = "Angola"
            }
            country.equals("AI") -> {
                pais = "Anguilla"
            }
            country.equals("AQ") -> {
                pais = "Antártica"
            }
            country.equals("AG") -> {
                pais = "Antigua e Bermuda"
            }
            country.equals("AR") -> {
                pais = "Argentina"
            }
            country.equals("AM") -> {
                pais = "Armênia"
            }
            country.equals("AW") -> {
                pais = "Aruba"
            }
            country.equals("AU") -> {
                pais = "Austrália"
            }
            country.equals("AT") -> {
                pais = "Áustria"
            }
            country.equals("AZ") -> {
                pais = "Azerbaijão"
            }
            country.equals("BS") -> {
                pais = "Bahamas"
            }
            country.equals("BH") -> {
                pais = "Bahrein"
            }
            country.equals("BD") -> {
                pais = "Bangladesh"
            }
            country.equals("BB") -> {
                pais = "Barbados"
            }
            country.equals("BY") -> {
                pais = "Bielorrúsia"
            }
            country.equals("BE") -> {
                pais = "Bélgica"
            }
            country.equals("BZ") -> {
                pais = "Belize"
            }
            country.equals("BJ") -> {
                pais = "Benin"
            }
            country.equals("BM") -> {
                pais = "Bermuda"
            }
            country.equals("BT") -> {
                pais = "Butão"
            }
            country.equals("BO") -> {
                pais = "Bolívia"
            }
            country.equals("BQ") -> {
                pais = "Bonaire"
            }
            country.equals("BA") -> {
                pais = "Bósnia e Herzergovina"
            }
            country.equals("BW") -> {
                pais = "Botsuana"
            }
            country.equals("BV") -> {
                pais = "Ilha Bouvet"
            }
            country.equals("IO") -> {
                pais = "Território Britânico do Oceano Índico"
            }
            country.equals("VG") -> {
                pais = "Ilhas Virgens Britânicas"
            }
            country.equals("BN") -> {
                pais = "Brunei"
            }
            country.equals("BG") -> {
                pais = "Bulgária"
            }
            country.equals("BF") -> {
                pais = "Burkina Faso"
            }
            country.equals("BI") -> {
                pais = "Burundi"
            }
            country.equals("KH") -> {
                pais = "Camboja"
            }
            country.equals("CM") -> {
                pais = "Camarões"
            }
            country.equals("CA") -> {
                pais = "Canadá"
            }
            country.equals("CV") -> {
                pais = "Cabo Verde"
            }
            country.equals("KY") -> {
                pais = "Ilhas Cayman"
            }
            country.equals("CF") -> {
                pais = "República Centro Africana"
            }
            country.equals("TD") -> {
                pais = "Chad"
            }
            country.equals("CL") -> {
                pais = "Chile"
            }
            country.equals("CN") -> {
                pais = "China"
            }
            country.equals("CX") -> {
                pais = "Ilha do Natal"
            }
            country.equals("CC") -> {
                pais = "Ilhas dos Cocos"
            }
            country.equals("CO") -> {
                pais = "Colômbia"
            }
            country.equals("KM") -> {
                pais = "Comores"
            }
            country.equals("CK") -> {
                pais = "Ilhas Cooks"
            }
            country.equals("CR") -> {
                pais = "Costa Rica"
            }
            country.equals("CI") -> {
                pais = "Costa do Marfin"
            }
            country.equals("HR") -> {
                pais = "Croácia"
            }
            country.equals("CU") -> {
                pais = "Cuba"
            }
            country.equals("CW") -> {
                pais = "Curaçao"
            }
            country.equals("CY") -> {
                pais = "Chipre"
            }
            country.equals("CZ") -> {
                pais = "República Tcheca"
            }
            country.equals("CD") -> {
                pais = "República Democrática do Congo"
            }
            country.equals("DK") -> {
                pais = "Dinamarca"
            }
            country.equals("DJ") -> {
                pais = "Djibouti"
            }
            country.equals("DM") -> {
                pais = "Dominica"
            }
            country.equals("DO") -> {
                pais = "República Dominicana"
            }
            country.equals("EC") -> {
                pais = "Equador"
            }
            country.equals("EG") -> {
                pais = "Egito"
            }
            country.equals("SV") -> {
                pais = "El Salvador"
            }
            country.equals("GQ") -> {
                pais = "Guiné Equatorial"
            }
            country.equals("ER") -> {
                pais = "Eritréia"
            }
            country.equals("EE") -> {
                pais = "Estônia"
            }
            country.equals("ET") -> {
                pais = "Etiópia"
            }
            country.equals("FK") -> {
                pais = "Ilhas Falklands"
            }
            country.equals("FO") -> {
                pais = "Ilhas Faroé"
            }
            country.equals("FM") -> {
                pais = "Estados Federados da Micronésia"
            }
            country.equals("FJ") -> {
                pais = "Ilhas Fiji"
            }
            country.equals("FI") -> {
                pais = "Finlândia"
            }
            country.equals("FR") -> {
                pais = "França"
            }
            country.equals("GF") -> {
                pais = "Guiana Francesa"
            }
            country.equals("PF") -> {
                pais = "Polinésia Francesa"
            }
            country.equals("TF") -> {
                pais = "Terras Austrais e Antárticas Francesas"
            }
            country.equals("GA") -> {
                pais = "Gabão"
            }
            country.equals("GM") -> {
                pais = "Gambia"
            }
            country.equals("GE") -> {
                pais = "Georgia"
            }
            country.equals("DE") -> {
                pais = "Alemanha"
            }
            country.equals("GH") -> {
                pais = "Gana"
            }
            country.equals("GI") -> {
                pais = "Gibraltar"
            }
            country.equals("GR") -> {
                pais = "Grécia"
            }
            country.equals("GL") -> {
                pais = "Groelândia"
            }
            country.equals("GD") -> {
                pais = "Granada"
            }
            country.equals("GP") -> {
                pais = "Guadalupe"
            }
            country.equals("GU") -> {
                pais = "Guam"
            }
            country.equals("GT") -> {
                pais = "Guatemala"
            }
            country.equals("GG") -> {
                pais = "Guernsey"
            }
            country.equals("GN") -> {
                pais = "Guiné"
            }
            country.equals("GW") -> {
                pais = "Guiné-Bissau"
            }
            country.equals("GY") -> {
                pais = "Guiana"
            }
            country.equals("HT") -> {
                pais = "Haití"
            }
            country.equals("HM") -> {
                pais = "Ilha Heard e Ilhas McDonald"
            }
            country.equals("HN") -> {
                pais = "Honduras"
            }
            country.equals("HK") -> {
                pais = "Hong Kong"
            }
            country.equals("HU") -> {
                pais = "Hungria"
            }
            country.equals("IS") -> {
                pais = "Islândia"
            }
            country.equals("IN") -> {
                pais = "Índia"
            }
            country.equals("ID") -> {
                pais = "Indonésia"
            }
            country.equals("IR") -> {
                pais = "Irã"
            }
            country.equals("IQ") -> {
                pais = "Iraque"
            }
            country.equals("IE") -> {
                pais = "Irlanda"
            }
            country.equals("IM") -> {
                pais = "Ilha de Man"
            }
            country.equals("IL") -> {
                pais = "Israel"
            }
            country.equals("IT") -> {
                pais = "Itália"
            }
            country.equals("JM") -> {
                pais = "Jamaica"
            }
            country.equals("JP") -> {
                pais = "Japão"
            }
            country.equals("JE") -> {
                pais = "Jersey"
            }
            country.equals("JO") -> {
                pais = "Jordânia"
            }
            country.equals("KZ") -> {
                pais = "Cazaquistão"
            }
            country.equals("KE") -> {
                pais = "Kênia"
            }
            country.equals("KI") -> {
                pais = "Quiribati"
            }
            country.equals("XK") -> {
                pais = "Kosovo"
            }
            country.equals("KW") -> {
                pais = "Kuait"
            }
            country.equals("KG") -> {
                pais = "Quirguistão"
            }
            country.equals("LA") -> {
                pais = "Laos"
            }
            country.equals("LV") -> {
                pais = "Letônia"
            }
            country.equals("LB") -> {
                pais = "Líbano"
            }
            country.equals("LS") -> {
                pais = "Lesoto"
            }
            country.equals("LR") -> {
                pais = "Libéria"
            }
            country.equals("LY") -> {
                pais = "Líbia"
            }
            country.equals("LI") -> {
                pais = "Liechtenstein"
            }
            country.equals("LT") -> {
                pais = "Lituânia"
            }
            country.equals("LU") -> {
                pais = "Luxemburgo"
            }
            country.equals("MO") -> {
                pais = "Macau"
            }
            country.equals("MK") -> {
                pais = "Macedônia do Norte"
            }
            country.equals("MG") -> {
                pais = "Madagascar"
            }
            country.equals("MW") -> {
                pais = "Malawi"
            }
            country.equals("MY") -> {
                pais = "Malásia"
            }
            country.equals("MV") -> {
                pais = "Maldivas"
            }
            country.equals("ML") -> {
                pais = "Mali"
            }
            country.equals("MT") -> {
                pais = "Malta"
            }
            country.equals("MH") -> {
                pais = "Ilhas Marshall"
            }
            country.equals("MQ") -> {
                pais = "Martinica"
            }
            country.equals("MR") -> {
                pais = "Mauritânia"
            }
            country.equals("MU") -> {
                pais = "Ilhas Maurício"
            }
            country.equals("YT") -> {
                pais = "Mayotte"
            }
            country.equals("MX") -> {
                pais = "México"
            }
            country.equals("MD") -> {
                pais = "Moldávia"
            }
            country.equals("MC") -> {
                pais = "Mônaco"
            }
            country.equals("MN") -> {
                pais = "Mongólia"
            }
            country.equals("ME") -> {
                pais = "Montenegro"
            }
            country.equals("MS") -> {
                pais = "Montserrat"
            }
            country.equals("MA") -> {
                pais = "Marrocos"
            }
            country.equals("MZ") -> {
                pais = "Moçambique"
            }
            country.equals("MM") -> {
                pais = "Mianmar"
            }
            country.equals("NA") -> {
                pais = "Namíbia"
            }
            country.equals("NR") -> {
                pais = "Nauru"
            }
            country.equals("NP") -> {
                pais = "Nepal"
            }
            country.equals("NL") -> {
                pais = "Países Baixos"
            }
            country.equals("NC") -> {
                pais = "Nova Caledônia"
            }
            country.equals("NZ") -> {
                pais = "Nova Zelândia"
            }
            country.equals("NI") -> {
                pais = "Nicarágua"
            }
            country.equals("NE") -> {
                pais = "Níger"
            }
            country.equals("NG") -> {
                pais = "Nigéria"
            }
            country.equals("NU") -> {
                pais = "Niue"
            }
            country.equals("NF") -> {
                pais = "Ilha Norfolk"
            }
            country.equals("KP") -> {
                pais = "Coréia do Norte"
            }
            country.equals("MP") -> {
                pais = "Northern Mariana Islands"
            }
            country.equals("NO") -> {
                pais = "Noruega"
            }
            country.equals("OM") -> {
                pais = "Omã"
            }
            country.equals("PK") -> {
                pais = "Paquistão"
            }
            country.equals("PW") -> {
                pais = "Palau"
            }
            country.equals("PS") -> {
                pais = "Palestina"
            }
            country.equals("PA") -> {
                pais = "Panamá"
            }
            country.equals("PG") -> {
                pais = "Papua Nova Guiné"
            }
            country.equals("PY") -> {
                pais = "Paraguai"
            }
            country.equals("PE") -> {
                pais = "Peru"
            }
            country.equals("PH") -> {
                pais = "Filipinas"
            }
            country.equals("PN") -> {
                pais = "Ilhas Pitcairn"
            }
            country.equals("PL") -> {
                pais = "Polônia"
            }
            country.equals("PT") -> {
                pais = "Portugal"
            }
            country.equals("PR") -> {
                pais = "Porto Rico"
            }
            country.equals("QA") -> {
                pais = "Catar"
            }
            country.equals("CG") -> {
                pais = "República do Congo"
            }
            country.equals("RE") -> {
                pais = "Ilha da Reunião"
            }
            country.equals("RE") -> {
                pais = "Romênia"
            }
            country.equals("RU") -> {
                pais = "Rússia"
            }
            country.equals("RW") -> {
                pais = "Ruanda"
            }
            country.equals("BL") -> {
                pais = "São Bartolomeu"
            }
            country.equals("SH") -> {
                pais = "Santa Helena"
            }
            country.equals("KN") -> {
                pais = "São Cristóvão e Névis"
            }
            country.equals("LC") -> {
                pais = "Santa Lúcia"
            }
            country.equals("MF") -> {
                pais = "São Martin"
            }
            country.equals("PM") -> {
                pais = "Saint-Pierre e Miquelon"
            }
            country.equals("VC") -> {
                pais = "São Vicente e Granadinas"
            }
            country.equals("WS") -> {
                pais = "Samoa"
            }
            country.equals("SM") -> {
                pais = "San Marino"
            }
            country.equals("ST") -> {
                pais = "São Tomé e Príncipe"
            }
            country.equals("SA") -> {
                pais = "Arábia Saudita"
            }
            country.equals("SN") -> {
                pais = "Senegal"
            }
            country.equals("RS") -> {
                pais = "Sévia"
            }
            country.equals("SC") -> {
                pais = "Seychelles"
            }
            country.equals("SL") -> {
                pais = "Serra Leoa"
            }
            country.equals("SG") -> {
                pais = "Singapura"
            }
            country.equals("SX") -> {
                pais = "São Martinho"
            }
            country.equals("SK") -> {
                pais = "Eslováquia"
            }
            country.equals("SI") -> {
                pais = "Eslovênia"
            }
            country.equals("SB") -> {
                pais = "Ilhas Salomão"
            }
            country.equals("SO") -> {
                pais = "Somália"
            }
            country.equals("ZA") -> {
                pais = "África do Sul"
            }
            country.equals("GS") -> {
                pais = "Ilhas Geórgia do Sul e Sandwich do Sul"
            }
            country.equals("KR") -> {
                pais = "Coréia do Sul"
            }
            country.equals("SS") -> {
                pais = "Sudão do Sul"
            }
            country.equals("ES") -> {
                pais = "Espanha"
            }
            country.equals("SP") -> {
                pais = "Ilhas Spratly"
            }
            country.equals("LK") -> {
                pais = "Sri Lanka"
            }
            country.equals("SD") -> {
                pais = "Sudão"
            }
            country.equals("SR") -> {
                pais = "Suriname"
            }
            country.equals("SJ") -> {
                pais = "Svalbard e Jan Mayen"
            }
            country.equals("SZ") -> {
                pais = "Essuatíni"
            }
            country.equals("SE") -> {
                pais = "Suécia"
            }
            country.equals("CH") -> {
                pais = "Suíça"
            }
            country.equals("SY") -> {
                pais = "Síria"
            }
            country.equals("TW") -> {
                pais = "Taiwan"
            }
            country.equals("TJ") -> {
                pais = "Tajiquistão"
            }
            country.equals("TZ") -> {
                pais = "Tanzânia"
            }
            country.equals("TH") -> {
                pais = "Tailândia"
            }
            country.equals("TL") -> {
                pais = "Timor Leste"
            }
            country.equals("TG") -> {
                pais = "Togo"
            }
        }


        binding.textoTemperatura.setText("${decimalFormatado.format(temp_c)}°C")
        binding.textoCidade.setText("$pais - $name")
        binding.textoInformacoes1.setText("Clima\n $descricao \n\n Umidade \n $humidity")
        binding.textoInformacoes2.setText("Temp.Min \n ${decimalFormatado.format(tempMin_c)} \n\n Temp.Max \n ${decimalFormatado.format(tempMax_c)}")
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_LOCATION = 1
        private const val TAG = "TelaPrincipal"
    }
    private fun recolherTeclado() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { viewTecaldo ->
            inputMethodManager.hideSoftInputFromWindow(viewTecaldo.windowToken, 0)
        }
    }
}