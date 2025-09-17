package ru.plastinin.weather_telegram_bot.service;

import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.model.WeatherData;

import java.util.List;

public interface WeatherService {
    String getWeather(double lat, double lon) throws ServiceException;

}
