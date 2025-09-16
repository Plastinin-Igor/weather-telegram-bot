package ru.plastinin.weather_telegram_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.plastinin.weather_telegram_bot.client.WeatherClient;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.model.WeatherData;

import java.util.Optional;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherClient client;

    @Override
    public String getWeather(String cityName) throws ServiceException {
        Optional<String> jsonOptional = client.getOpenWeatherMapData(cityName);
        String jsonString = jsonOptional.orElseThrow(() -> new ServiceException("Не удалось получить данные."));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WeatherData weatherData = objectMapper.readValue(jsonString, WeatherData.class);

            String text = "Погода в Москве: \n" + weatherData.getMain().toStringMain();

            return text;
        } catch (JsonProcessingException e) {
            log.error("Ошибка преобразования данных: {}", e.getMessage());
            throw new ServiceException("Ошибка преобразования данных.", e);
        }
    }

}
