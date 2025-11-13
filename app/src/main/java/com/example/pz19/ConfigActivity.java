package com.example.pz19;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CITY = "widget_city_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Проверяем корректность ID
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // Формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // Отрицательный ответ по умолчанию
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.activity_config);
    }

    public void setCity(View view) {
        EditText eText = findViewById(R.id.city);

        // Записываем значения в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(WIDGET_CITY + widgetID, eText.getText().toString());
        editor.apply();

        // Обновляем виджет - ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЙ МЕТОД
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidget.updateAppWidget(this, sp, appWidgetManager, widgetID);

        // Положительный ответ
        setResult(RESULT_OK, resultValue);
        finish();
    }
}