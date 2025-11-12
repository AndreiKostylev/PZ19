package com.example.pz19;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView cityField;
    private TextView updatedField;
    private ImageView weatherIcon;
    private TextView detailsField;
    private TextView currentTemperatureField;
    private Button refreshButton;

    private int cityIndex = 0;
    private String[] cities = {"Москва", "Санкт-Петербург", "Оренбург", "Екатеринбург"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        loadWeatherData();
    }

    private void initViews() {
        cityField = findViewById(R.id.city_field);
        updatedField = findViewById(R.id.updated_field);
        weatherIcon = findViewById(R.id.weather_icon);
        detailsField = findViewById(R.id.details_field);
        currentTemperatureField = findViewById(R.id.current_temperature_field);
        refreshButton = findViewById(R.id.refreshButton);
    }

    private void setupClickListeners() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityIndex = (cityIndex + 1) % cities.length;
                loadWeatherData();
            }
        });
    }

    private void loadWeatherData() {
        String currentCity = cities[cityIndex];

        // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД
        ConnectFetch.loadWeatherData(MainActivity.this, currentCity, new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                renderWeather(response);
            }

            @Override
            public void onFail(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void renderWeather(JSONObject json) {
        try {
            cityField.setText(StaticWeatherAnalize.getCityField(json));
            updatedField.setText(StaticWeatherAnalize.getLastUpdateTime(json));
            detailsField.setText(StaticWeatherAnalize.getDetailsField(json));
            currentTemperatureField.setText(StaticWeatherAnalize.getTemperatureField(json));

            String iconUrl = StaticWeatherAnalize.getIconUrl(json);
            Glide.with(this)
                    .load(iconUrl)
                    .into(weatherIcon);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка отображения данных", Toast.LENGTH_SHORT).show();
        }
    }
}