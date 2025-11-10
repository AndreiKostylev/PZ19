package com.example.pz19;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import android.util.Log;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private TextView cityField, updatedField, detailsField, temperatureField;
    private ImageView weatherIcon;
    private Button refreshButton;
    private int currentCityIndex = 0;

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();


        cityField = findViewById(R.id.city_field);
        updatedField = findViewById(R.id.updated_field);
        detailsField = findViewById(R.id.details_field);
        temperatureField = findViewById(R.id.current_temperature_field);
        weatherIcon = findViewById(R.id.weather_icon);
        refreshButton = findViewById(R.id.refreshButton);


        cityField.setText("Загрузка...");
        updatedField.setText("Подключение к Яндекс.Погоде");
        detailsField.setText("Получение данных...");
        temperatureField.setText("-- °C");


        refreshButton.setOnClickListener(v -> {
            currentCityIndex = (currentCityIndex + 1) % 6;
            Log.d(LOG_TAG, "🔄 Switching to city index: " + currentCityIndex);
            updateWeatherData(currentCityIndex);
        });


        handler.postDelayed(() -> {
            updateWeatherData(0);
        }, 1000);
    }

    private void updateWeatherData(final int cityIndex) {
        String[] cityNames = {"Москва", "Санкт-Петербург", "Казань", "Екатеринбург", "Новосибирск", "Оренбург"};


        cityField.setText("Загрузка: " + cityNames[cityIndex]);
        updatedField.setText("Обновление...");
        detailsField.setText("Получение данных с сервера");
        temperatureField.setText("...");

        new Thread() {
            public void run() {
                try {
                    Log.d(LOG_TAG, "📍 Starting API request for: " + cityNames[cityIndex]);
                    final JSONObject json = ConnectFetch.getWeatherData(MainActivity.this, cityIndex);

                    handler.post(new Runnable() {
                        public void run() {
                            if (json != null) {
                                renderWeather(json);
                            } else {
                                showError();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG_TAG, "💥 Thread exception: " + e.getMessage());
                    handler.post(() -> showError());
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {

            cityField.setText(StaticWeatherAnalize.getCityField(json));
            updatedField.setText(StaticWeatherAnalize.getLastUpdateTime(json));
            detailsField.setText(StaticWeatherAnalize.getDetailsField(json));
            temperatureField.setText(StaticWeatherAnalize.getTemperatureField(json));


            String iconUrl = StaticWeatherAnalize.getIconUrl(json);
            Log.d(LOG_TAG, "🖼 Loading icon from: " + iconUrl);

            Glide.with(this)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(weatherIcon);

            Log.d(LOG_TAG, "✅ Weather data rendered successfully");

        } catch (Exception e) {
            Log.e(LOG_TAG, "❌ Error rendering weather: " + e.getMessage());
            showError();
        }
    }

    private void showError() {
        cityField.setText("Ошибка загрузки");
        updatedField.setText("Проверьте подключение");
        detailsField.setText("Нет данных\nПопробуйте еще раз");
        temperatureField.setText("-- °C");
        Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
    }
}