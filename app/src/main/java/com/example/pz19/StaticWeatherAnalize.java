package com.example.pz19;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import android.util.Log;

public class StaticWeatherAnalize {

    public static String getCityField(JSONObject json) {
        try {
            Log.d("StaticWeatherAnalize", "🔍 Parsing city from JSON...");

            // ПРОСТОЙ И НАДЕЖНЫЙ СПОСОБ - используем city_name который добавляет ConnectFetch
            if (json.has("city_name")) {
                String cityName = json.getString("city_name");
                Log.d("StaticWeatherAnalize", "✅ Found city_name: " + cityName);
                return cityName.toUpperCase() + ", RU";
            }

            // Альтернативные варианты на случай если city_name нет
            if (json.has("geo_object")) {
                JSONObject geoObject = json.getJSONObject("geo_object");
                if (geoObject.has("locality")) {
                    String cityName = geoObject.getJSONObject("locality").getString("name");
                    Log.d("StaticWeatherAnalize", "✅ Found locality name: " + cityName);
                    return cityName.toUpperCase() + ", RU";
                }
            }

            // Если ничего не нашли
            Log.e("StaticWeatherAnalize", "❌ No city found in JSON");
            return "МОСКВА, RU";

        } catch (Exception e) {
            Log.e("StaticWeatherAnalize", "💥 Error getting city: " + e.getMessage());
            e.printStackTrace();
            return "МОСКВА, RU";
        }
    }

    public static String getLastUpdateTime(JSONObject json) {
        try {
            DateFormat df = DateFormat.getDateTimeInstance();
            return "Обновлено: " + df.format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "НЕТ ДАННЫХ";
    }

    public static String getDetailsField(JSONObject json) {
        try {
            JSONObject fact = json.getJSONObject("fact");
            String condition = getConditionText(fact.getString("condition"));
            int humidity = fact.getInt("humidity");
            int pressure = fact.getInt("pressure_mm");
            double windSpeed = fact.getDouble("wind_speed");

            return condition.toUpperCase() +
                    "\nВлажность: " + humidity + "%" +
                    "\nДавление: " + pressure + " мм рт.ст." +
                    "\nВетер: " + windSpeed + " м/с";
        } catch (Exception e) {
            e.printStackTrace();
            return "НЕТ ДАННЫХ";
        }
    }

    public static String getTemperatureField(JSONObject json) {
        try {
            JSONObject fact = json.getJSONObject("fact");
            int temp = fact.getInt("temp");
            int feelsLike = fact.getInt("feels_like");
            return temp + " °C\nОщущается как: " + feelsLike + " °C";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "НЕТ ДАННЫХ";
    }

    public static String getIconUrl(JSONObject json) {
        try {
            JSONObject fact = json.getJSONObject("fact");
            String condition = fact.getString("condition");
            return "https://yastatic.net/weather/i/icons/funky/dark/" + condition + ".svg";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "https://yastatic.net/weather/i/icons/funky/dark/clear.svg";
    }

    public static String getConditionText(String condition) {
        switch (condition) {
            case "clear": return "ясно";
            case "partly-cloudy": return "малооблачно";
            case "cloudy": return "облачно";
            case "overcast": return "пасмурно";
            case "drizzle": return "морось";
            case "light-rain": return "небольшой дождь";
            case "rain": return "дождь";
            case "moderate-rain": return "умеренный дождь";
            case "heavy-rain": return "сильный дождь";
            case "showers": return "ливень";
            case "wet-snow": return "дождь со снегом";
            case "light-snow": return "небольшой снег";
            case "snow": return "снег";
            case "snow-showers": return "снегопад";
            case "hail": return "град";
            case "thunderstorm": return "гроза";
            case "thunderstorm-with-rain": return "дождь с грозой";
            case "thunderstorm-with-hail": return "гроза с градом";
            default: return condition;
        }
    }
}