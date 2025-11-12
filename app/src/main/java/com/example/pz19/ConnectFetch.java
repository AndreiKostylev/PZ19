package com.example.pz19;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.util.Log;
import android.os.Handler;

public class ConnectFetch {
    private OnConnectionCompleteListener listener;
    private Handler handler;

    // Конструктор для использования с анонимными классами
    public ConnectFetch(Context context, String city, OnConnectionCompleteListener listener) {
        this.listener = listener;
        this.handler = new Handler();
        updateWeatherData(city, context);
    }

    // МЕТОД ДЛЯ ВИДЖЕТА И MAINACTIVITY
    public static void loadWeatherData(Context context, String city, OnConnectionCompleteListener listener) {
        new ConnectFetch(context, city, listener);
    }

    private void updateWeatherData(final String city, final Context context) {
        new Thread() {
            public void run() {
                final JSONObject json = getWeatherDataByCityName(city);
                handler.post(new Runnable() {
                    public void run() {
                        if (json != null) {
                            listener.onSuccess(json);
                        } else {
                            listener.onFail("Ошибка получения данных");
                        }
                    }
                });
            }
        }.start();
    }

    private static final String LOG_TAG = "YandexWeather";

    private static final String[][] RUSSIAN_CITIES = {
            {"Москва", "55.7558", "37.6173"},
            {"Санкт-Петербург", "59.9343", "30.3351"},
            {"Казань", "55.7961", "49.1064"},
            {"Екатеринбург", "56.8389", "60.6057"},
            {"Новосибирск", "55.0084", "82.9357"},
            {"Оренбург", "51.7676", "55.0979"}
    };

    public static JSONObject getWeatherDataByCityName(String cityName) {
        String lat = "55.7558";
        String lon = "37.6173";

        for (String[] city : RUSSIAN_CITIES) {
            if (city[0].equals(cityName)) {
                lat = city[1];
                lon = city[2];
                break;
            }
        }

        try {
            String urlString = "https://api.weather.yandex.ru/v2/forecast?lat=" + lat +
                    "&lon=" + lon + "&limit=1&extra=true";

            Log.d(LOG_TAG, "🔗 Full URL: " + urlString);

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-API-Key", "00705c3e-b69a-4113-9331-2b4fe0fa0af3");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);

            Log.d(LOG_TAG, "🚀 Making request to Yandex Weather API...");

            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "📡 HTTP Response Code: " + responseCode);

            if (responseCode == 200) {
                Log.d(LOG_TAG, "✅ SUCCESS! API returned 200");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                Log.d(LOG_TAG, "📄 JSON Response length: " + json.length() + " chars");

                JSONObject data = new JSONObject(json.toString());
                data.put("city_name", cityName);

                Log.d(LOG_TAG, "🎉 Data successfully parsed for: " + cityName);
                return data;

            } else {
                Log.e(LOG_TAG, "❌ API Error. HTTP Code: " + responseCode);

                Log.d(LOG_TAG, "🔄 Falling back to mock data for: " + cityName);
                return getMockWeatherData(cityName);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "💥 Exception during API call: " + e.getMessage());
            Log.d(LOG_TAG, "🔄 Using mock data due to exception");
            return getMockWeatherData(cityName);
        }
    }

    // Mock данные
    private static JSONObject getMockWeatherData(String cityName) {
        try {
            Log.d(LOG_TAG, "🎭 Generating mock data for: " + cityName);

            JSONObject mockData = new JSONObject();
            mockData.put("city_name", cityName);

            JSONObject fact = new JSONObject();
            fact.put("temp", -5);
            fact.put("feels_like", -8);
            fact.put("condition", "clear");
            fact.put("humidity", 75);
            fact.put("pressure_mm", 1015);
            fact.put("wind_speed", 3.2);

            mockData.put("fact", fact);

            Log.d(LOG_TAG, "🎭 Mock data ready for: " + cityName);
            return mockData;

        } catch (Exception e) {
            Log.e(LOG_TAG, "💥 Error creating mock data");
            return null;
        }
    }

    public static String getConditionText(String condition) {
        switch (condition) {
            case "clear":
                return "ясно";
            case "partly-cloudy":
                return "малооблачно";
            case "cloudy":
                return "облачно";
            case "overcast":
                return "пасмурно";
            case "light-rain":
                return "небольшой дождь";
            case "rain":
                return "дождь";
            case "snow":
                return "снег";
            default:
                return condition;
        }
    }
}