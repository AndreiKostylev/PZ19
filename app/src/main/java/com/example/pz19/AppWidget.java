package com.example.pz19;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import org.json.JSONObject;
import java.util.Arrays;

public class AppWidget extends AppWidgetProvider {
    static final String LOG_TAG = "myLogs";

    // ОСНОВНОЙ МЕТОД для обновления виджета (без SharedPreferences)
    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        // Читаем настройки для этого виджета
        SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        String widgetCity = sp.getString(ConfigActivity.WIDGET_CITY + appWidgetId, "Москва");

        updateAppWidgetInternal(context, widgetCity, appWidgetManager, appWidgetId);
    }

    // МЕТОД для ConfigActivity (с SharedPreferences)
    public static void updateAppWidget(Context context, SharedPreferences sharedPreferences,
                                       AppWidgetManager appWidgetManager, int appWidgetId) {
        // Читаем параметры Preferences
        String widgetCity = sharedPreferences.getString(ConfigActivity.WIDGET_CITY + appWidgetId, null);
        if (widgetCity == null) {
            Log.e(LOG_TAG, "❌ No city found for widget: " + appWidgetId);
            return;
        }

        Log.d(LOG_TAG, "🔧 Configuring widget " + appWidgetId + " for city: " + widgetCity);
        updateAppWidgetInternal(context, widgetCity, appWidgetManager, appWidgetId);
    }

    // ВНУТРЕННИЙ МЕТОД для обновления с конкретным городом
    private static void updateAppWidgetInternal(final Context context, final String city,
                                                final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        // Настраиваем клик для открытия приложения
        setupClickIntent(context, views);

        // Устанавливаем начальные значения
        views.setTextViewText(R.id.city_field, city);
        views.setTextViewText(R.id.details_field, "Загрузка...");

        // Сразу обновляем виджет с заглушкой
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Загружаем актуальные данные
        ConnectFetch.loadWeatherData(context, city, new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                renderWeather(response, context, views, appWidgetId);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                Log.d(LOG_TAG, "✅ Widget updated successfully for: " + city);
            }

            @Override
            public void onFail(String message) {
                views.setTextViewText(R.id.details_field, "Ошибка данных");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                Log.e(LOG_TAG, "❌ Widget update failed for " + city + ": " + message);
            }
        });
    }

    // Отрисовка погоды в виджете
    private static void renderWeather(JSONObject json, Context context, RemoteViews remoteViews, int appWidgetId) {
        try {
            // Получаем город
            String cityName = StaticWeatherAnalize.getCityField(json);

            // Получаем погодные данные
            JSONObject fact = json.getJSONObject("fact");
            int temp = fact.getInt("temp");
            String condition = fact.getString("condition");
            String conditionText = StaticWeatherAnalize.getConditionText(condition);

            // Устанавливаем данные в виджет
            remoteViews.setTextViewText(R.id.city_field, cityName);
            remoteViews.setTextViewText(R.id.details_field, temp + "°C\n" + conditionText);

            Log.d("AppWidget", "✅ Widget rendered: " + cityName + " " + temp + "°C");

        } catch (Exception e) {
            Log.e("AppWidget", "❌ Error rendering widget: " + e.getMessage());
            remoteViews.setTextViewText(R.id.city_field, "Москва");
            remoteViews.setTextViewText(R.id.details_field, "Нет данных");
        }
    }

    // Настройка клика по виджету
    private static void setupClickIntent(Context context, RemoteViews views) {
        try {
            Log.d(LOG_TAG, "🔗 Setting up click intent...");

            // Создаем интент для открытия MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Создаем PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Устанавливаем обработчик клика на весь виджет
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            Log.d(LOG_TAG, "✅ Click intent setup complete");

        } catch (Exception e) {
            Log.e(LOG_TAG, "❌ Error setting up click intent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));

        // Обновляем все экземпляры виджета
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Удаляем настройки удаленных виджетов
        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();

        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_CITY + widgetID);
            Log.d(LOG_TAG, "🗑️ Removed preferences for widget: " + widgetID);
        }
        editor.apply();

        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(LOG_TAG, "onEnabled - widget added to home screen");
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOG_TAG, "onDisabled - all widgets removed from home screen");

        // Очищаем все настройки виджетов
        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        Log.d(LOG_TAG, "🧹 All widget preferences cleared");
    }
}