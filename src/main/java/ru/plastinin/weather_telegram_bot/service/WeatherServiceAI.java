package ru.plastinin.weather_telegram_bot.service;

import ru.plastinin.weather_telegram_bot.exception.ServiceException;


public interface WeatherServiceAI {

    String getWeather(double lat, double lon) throws ServiceException;

}