package ru.plastinin.weather_telegram_bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WeatherData {

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("weather")
    private Weather[] weather;

    @JsonProperty("base")
    private String base;

    @JsonProperty("main")
    private Main main;

    @JsonProperty("visibility")
    private int visibility;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("clouds")
    private Clouds clouds;

    @JsonProperty("dt")
    private long dt;

    @JsonProperty("sys")
    private Sys sys;

    @JsonProperty("timezone")
    private int timezone;

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("cod")
    private int cod;


    @Data
    public static class Coord {

        @JsonProperty("lon")
        private double lon;

        @JsonProperty("lat")
        private double lat;

    }

    @Data
    public static class Weather {

        @JsonProperty("id")
        private int id;

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        @JsonProperty("icon")
        private String icon;

    }

    @Data
    public static class Main {

        @JsonProperty("temp")
        private double temp;

        @JsonProperty("feels_like")
        private double feelsLike;

        @JsonProperty("temp_min")
        private double tempMin;

        @JsonProperty("temp_max")
        private double tempMax;

        @JsonProperty("pressure")
        private int pressure;

        @JsonProperty("humidity")
        private int humidity;

        @JsonProperty("sea_level")
        private int seaLevel;

        @JsonProperty("grnd_level")
        private int grndLevel;

        public String toStringMain() {
            return "Температура: " + temp + "°C" +
                   "\nОщущается как: " + feelsLike + "°C" +
                   "\nМинимум: " + tempMin + "°C" +
                   "\nМаксимум: " + tempMax + "°C" +
                   "\nДавление: " + pressure + " мм.р.ст." +
                   "\nВлажность воздуха :" + humidity + "%";
        }

    }

    @Data
    public static class Wind {

        @JsonProperty("speed")
        private double speed;

        @JsonProperty("deg")
        private int deg;

        @JsonProperty("gust")
        private double gust;
    }

    @Data
    public static class Clouds {

        @JsonProperty("all")
        private int all;
    }

    @Data
    public static class Sys {

        @JsonProperty("type")
        private int type;

        @JsonProperty("id")
        private int id;

        @JsonProperty("country")
        private String country;

        @JsonProperty("sunrise")
        private long sunrise;

        @JsonProperty("sunset")
        private long sunset;

    }
}
