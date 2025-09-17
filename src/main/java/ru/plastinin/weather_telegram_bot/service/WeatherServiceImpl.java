package ru.plastinin.weather_telegram_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.plastinin.weather_telegram_bot.client.WeatherClient;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.model.WeatherData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherClient client;

    private List<WeatherData.Weather> weathers;

    @Override
    public String getWeather(double lat, double lon) throws ServiceException {
        Optional<String> jsonOptional = client.getOpenWeatherMapData(lat, lon);
        String jsonString = jsonOptional.orElseThrow(() -> new ServiceException("Unable to retrieve data " +
                                                                                "from weather service Openweathermap."));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WeatherData weatherData = objectMapper.readValue(jsonString, WeatherData.class);

            weathers = List.of(weatherData.getWeather());
            String weatherDescription = getIconWeatherURL(weathers);

            String text = """
                    🗺️%s: %s
                    
                    Температура 🌡️ %s °C
                    Ощущается как 🤔 %s °C
                    Минимальная 📉 %s °C
                    Максимальная 📈️ %s °C
                    Атмосферное Давление 🌐 %s кПа
                    Влажность воздуха 💧 %s %%
                    
                    Команды:
                    Начало работы 🚀 /start
                    Справка 🔍 /help
                    """;

            return String.format(text, weatherData.getName(), weatherDescription, weatherData.getMain().getTemp(), weatherData.getMain().getFeelsLike(),
                    weatherData.getMain().getTempMin(), weatherData.getMain().getTempMax(), weatherData.getMain().getPressure(),
                    weatherData.getMain().getHumidity());

        } catch (JsonProcessingException e) {
            log.error("Ошибка преобразования данных: {}", e.getMessage());
            throw new ServiceException("Ошибка преобразования данных.", e);
        }
    }


    private String getIconWeatherURL(List<WeatherData.Weather> weathers) {
        // String url = "http://openweathermap.org/img/w/";
        //return weathers.stream().map(i -> "<a href=\"" + url + i.getIcon() + ".png\"> </a>")
        //        .collect(Collectors.joining(" "));

        return weathers.stream().map(WeatherData.Weather::getDescription)
                .collect(Collectors.joining(" "));
    }
}
