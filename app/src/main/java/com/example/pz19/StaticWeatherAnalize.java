package com.example.pz19;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
public class StaticWeatherAnalize {
    public static String getCityField(JSONObject json) {
        try {
            return json.getString("city_name").toUpperCase() + ", RU";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "НЕИЗВЕСТНО";
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
        }
        return "НЕТ ДАННЫХ";
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
            case "clear":
                return "ясно";
            case "partly-cloudy":
                return "малооблачно";
            case "cloudy":
                return "облачно";
            case "overcast":
                return "пасмурно";
            case "drizzle":
                return "морось";
            case "light-rain":
                return "небольшой дождь";
            case "rain":
                return "дождь";
            case "moderate-rain":
                return "умеренный дождь";
            case "heavy-rain":
                return "сильный дождь";
            case "showers":
                return "ливень";
            case "wet-snow":
                return "дождь со снегом";
            case "light-snow":
                return "небольшой снег";
            case "snow":
                return "снег";
            case "snow-showers":
                return "снегопад";
            case "hail":
                return "град";
            case "thunderstorm":
                return "гроза";
            case "thunderstorm-with-rain":
                return "дождь с грозой";
            case "thunderstorm-with-hail":
                return "гроза с градом";
            default:
                return condition;
        }
    }
}
