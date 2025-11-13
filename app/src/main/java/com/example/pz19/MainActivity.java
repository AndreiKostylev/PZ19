package com.example.pz19;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        setInfo();
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
                setInfo();
            }
        });

        // Клик по названию города для его изменения
        cityField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    private void setInfo() {
        String currentCity = new CityPreference(this).getCity();

        // Логи для отладки
        Log.d("MainActivity", "🔍 Loading weather for city: " + currentCity);

        ConnectFetch.loadWeatherData(MainActivity.this, currentCity, new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("MainActivity", "✅ Data received successfully");
                renderWeather(response);
            }

            @Override
            public void onFail(String message) {
                Log.e("MainActivity", "❌ Data load failed: " + message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void renderWeather(JSONObject json) {
        try {
            Log.d("MainActivity", "🎨 Starting renderWeather");

            String city = StaticWeatherAnalize.getCityField(json);
            Log.d("MainActivity", "🏙️ City to display: " + city);

            cityField.setText(city);
            updatedField.setText(StaticWeatherAnalize.getLastUpdateTime(json));
            detailsField.setText(StaticWeatherAnalize.getDetailsField(json));
            currentTemperatureField.setText(StaticWeatherAnalize.getTemperatureField(json));

            String iconUrl = StaticWeatherAnalize.getIconUrl(json);
            Log.d("MainActivity", "🖼️ Icon URL: " + iconUrl);

            Glide.with(this)
                    .load(iconUrl)
                    .into(weatherIcon);

            Log.d("MainActivity", "✅ renderWeather completed successfully");

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error in renderWeather: " + e.getMessage());
            e.printStackTrace();

            // Показываем значения по умолчанию при ошибке
            cityField.setText("МОСКВА, RU");
            updatedField.setText("Обновлено: только что");
            detailsField.setText("Данные временно недоступны");
            currentTemperatureField.setText("-- °C");
        }
    }

    // Диалог для смены города
    private void showInputDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Измените город:");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Сохранить", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // Смена города с синхронизацией виджетов
    public void changeCity(String city) {
        if (city != null && !city.trim().isEmpty()) {
            Log.d("MainActivity", "🔧 Changing city to: " + city);

            // Сохраняем город для приложения
            new CityPreference(this).setCity(city);

            // Проверяем сохранение
            String savedCity = new CityPreference(this).getCity();
            Log.d("MainActivity", "💾 City saved as: " + savedCity);

            // Обновляем приложение
            setInfo();

            // Синхронизируем ВСЕ виджеты
            updateAllWidgets(city);

            Toast.makeText(this, "Город изменен на: " + city, Toast.LENGTH_SHORT).show();
        }
    }

    // Обновить все виджеты
    private void updateAllWidgets(String city) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName appWidget = new ComponentName(this, AppWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);

            Log.d("MainActivity", "🔄 Found " + appWidgetIds.length + " widgets to update");

            if (appWidgetIds.length > 0) {
                SharedPreferences sp = getSharedPreferences(ConfigActivity.WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                // Обновляем город для всех виджетов
                for (int appWidgetId : appWidgetIds) {
                    editor.putString(ConfigActivity.WIDGET_CITY + appWidgetId, city);
                    Log.d("MainActivity", "📝 Updated widget " + appWidgetId + " to city: " + city);
                }
                editor.apply();

                // Принудительно обновляем виджеты
                for (int appWidgetId : appWidgetIds) {
                    AppWidget.updateAppWidget(MainActivity.this, appWidgetManager, appWidgetId);
                }

                Log.d("MainActivity", "✅ Successfully updated " + appWidgetIds.length + " widgets");
                Toast.makeText(this, "Обновлено " + appWidgetIds.length + " виджетов", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("MainActivity", "ℹ️ No widgets found to update");
            }

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error updating widgets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}