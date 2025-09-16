package ru.plastinin.weather_telegram_bot.service;

import ru.plastinin.weather_telegram_bot.exception.ServiceException;

public interface WeatherService {
    String getWeather(String cityName) throws ServiceException;
}
