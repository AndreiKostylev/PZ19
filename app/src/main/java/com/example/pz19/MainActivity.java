package com.example.pz19;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private TextView weatherTextView;
    private Button refreshButton;
    private int currentCityIndex = 0;

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        weatherTextView = findViewById(R.id.weather);
        refreshButton = findViewById(R.id.refreshButton);


        updateWeatherData(currentCityIndex);


        refreshButton.setOnClickListener(v -> {
            currentCityIndex = (currentCityIndex + 1) % 6;
            updateWeatherData(currentCityIndex);
        });
    }

    private void updateWeatherData(final int cityIndex) {
        weatherTextView.setText("Загрузка погоды...\n\nПодключение к Яндекс.Погоде");

        new Thread() {
            public void run() {
                Log.d(LOG_TAG, "Fetching weather for city index: " + cityIndex);
                final JSONObject json = ConnectFetch.getWeatherData(MainActivity.this, cityIndex);

                handler.post(new Runnable() {
                    public void run() {
                        if (json != null) {
                            renderWeather(json);
                        } else {
                            weatherTextView.setText("Ошибка загрузки данных\nПроверьте API ключ");
                            Toast.makeText(MainActivity.this,
                                    "Не удалось получить данные от Яндекс.Погоды",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {
            String cityName = json.getString("city_name");
            JSONObject fact = json.getJSONObject("fact");

            int temp = fact.getInt("temp");
            int feelsLike = fact.getInt("feels_like");
            String condition = ConnectFetch.getConditionText(fact.getString("condition"));
            int humidity = fact.getInt("humidity");
            int pressure = fact.getInt("pressure_mm");
            double windSpeed = fact.getDouble("wind_speed");

            String weatherText = String.format(
                    "🏙 %s\n\n🌡 Температура: %d°C\n🥶 Ощущается как: %d°C\n☁ Погода: %s\n💧 Влажность: %d%%\n📊 Давление: %d мм рт.ст.\n💨 Ветер: %.1f м/с",
                    cityName, temp, feelsLike, condition, humidity, pressure, windSpeed
            );

            weatherTextView.setText(weatherText);
            Log.d(LOG_TAG, "Weather displayed for: " + cityName);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error rendering weather: " + e.getMessage());
            weatherTextView.setText("Ошибка обработки данных\n" + e.getMessage());
        }
    }
}