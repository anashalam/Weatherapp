package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val apiKey = "fb8ccb5caedd3b29bf194e68f0dc115d" // Replace with your OpenWeatherMap API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cityInput = findViewById<EditText>(R.id.cityInput)
        val locationText = findViewById<TextView>(R.id.locationText)
        val tempText = findViewById<TextView>(R.id.tempText)
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)

        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyForecastLayout)

        fetchWeather("Delhi", locationText, tempText, descriptionText, mainLayout, hourlyLayout)

        cityInput.setOnEditorActionListener { v, actionId, event ->
            val city = cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeather(city, locationText, tempText, descriptionText, mainLayout, hourlyLayout)
            }
            true
        }
    }

    private fun fetchWeather(
        city: String,
        locationText: TextView,
        tempText: TextView,
        descriptionText: TextView,
        layout: ConstraintLayout,
        hourlyLayout: LinearLayout
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url =
                    "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
                val request = Request.Builder().url(url).build()
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                val json = JSONObject(response.body!!.string())
                val main = json.getJSONObject("main")
                val weather = json.getJSONArray("weather").getJSONObject(0)
                val temp = main.getDouble("temp").toInt()
                val desc = weather.getString("main")

                withContext(Dispatchers.Main) {
                    locationText.text = "📍 $city"
                    tempText.text = "$temp°"
                    descriptionText.text = "It's $desc"

                    // Background & Theme Change
                    when (desc) {
                        "Clear" -> layout.setBackgroundResource(R.drawable.bg_clear_sky)
                        "Clouds" -> layout.setBackgroundResource(R.drawable.bg_cloudy)
                        "Rain" -> layout.setBackgroundResource(R.drawable.bg_rain)
                        "Snow" -> layout.setBackgroundResource(R.drawable.bg_snow)
                        else -> layout.setBackgroundResource(R.drawable.bg_default)
                    }

                    // (Optional) Update hourly forecast dummy data
                    updateHourlyForecast(hourlyLayout)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "City not found or error fetching data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun updateHourlyForecast(layout: LinearLayout) {
        layout.removeAllViews()
        val hours = listOf("06:00 AM", "09:00 AM", "12:00 PM", "03:00 PM", "06:00 PM")
        val temps = listOf(22, 24, 27, 25, 23)
        val icons = listOf(
            R.drawable.sun,
            R.drawable.clouds,
            R.drawable.rain,
         R.drawable.snowman
        )

        for (i in hours.indices) {
            val item = layoutInflater.inflate(R.layout.forecast_item, layout, false)
            item.findViewById<ImageView>(R.id.iconView).setImageResource(icons[i])
            item.findViewById<TextView>(R.id.timeText).text = hours[i]
            item.findViewById<TextView>(R.id.tempText).text = "${temps[i]}°"
            layout.addView(item)
        }
    }
}
