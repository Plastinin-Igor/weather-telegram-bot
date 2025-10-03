package ru.plastinin.weather_telegram_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.plastinin.weather_telegram_bot.client.WeatherClient;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.model.WeatherData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherClient client;

    // private List<WeatherData.Weather> weathers;

    @Override
    public String getWeather(double lat, double lon) throws ServiceException {
        List<WeatherData.Weather> weathers;
        Optional<String> jsonOptional = client.getOpenWeatherMapData(lat, lon);
        String jsonString = jsonOptional.orElseThrow(() -> new ServiceException("Unable to retrieve data " +
                                                                                "from weather service Openweathermap."));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WeatherData weatherData = objectMapper.readValue(jsonString, WeatherData.class);

            weathers = List.of(weatherData.getWeather());
            String weatherDescription = getWeatherDescription(weathers);

            String text = """
                    <b>%s: %s</b>
                    
                    🌡<i>Температура</i>: ️<b>%s °C</b>
                    🤔<i>Ощущается как</i>: <b>%s °C</b>
                    📉<i>Минимальная</i>: <b>%s °C</b>
                    📈️<i>Максимальная</i>: <b>%s °C</b>
                    🌐<i>Атм Давление</i>: <b>%s мм р.с</b>
                    💧<i>Влажность</i>: <b>%s %%</b>
                    
                    <i>Ветер</i>: <b>%s%s</b>
                    
                    %s
                    
                    <b>Команды:</b>
                    Начало работы 🚀 /start
                    Справка 🔍 /help
                    """;

            return String.format(text,
                    weatherData.getName(),
                    weatherDescription,
                    weatherData.getMain().getTemp(),
                    weatherData.getMain().getFeelsLike(),
                    weatherData.getMain().getTempMin(),
                    weatherData.getMain().getTempMax(),
                    weatherData.getMain().getPressure() / 10 * 7.5, // кПа -> мм р.ст.
                    weatherData.getMain().getHumidity(),
                    windDirectionFromDegrees(weatherData.getWind().getDeg()),
                    windDescription(weatherData.getWind().getSpeed()),
                    sunriseSunsetTime(weatherData.getSys().getSunrise(),
                            weatherData.getSys().getSunset(),
                            weatherData.getTimezone()));

        } catch (JsonProcessingException e) {
            log.error("Ошибка преобразования данных: {}", e.getMessage());
            throw new ServiceException("Ошибка преобразования данных.", e);
        }
    }

    private String getWeatherDescription(List<WeatherData.Weather> weathers) {
        return weathers.stream().map(WeatherData.Weather::getDescription)
                .collect(Collectors.joining(" "));
    }

    /**
     * Время Восхода/заката
     */
    private String sunriseSunsetTime(Long sunrise, Long sunset, int timeZoneSeconds) {
        // Формат даты
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Time zone
        // Значение timeZoneSeconds представляет собой смещение временной зоны относительно UTC в секундах.
        int hoursOffSet = Math.abs(timeZoneSeconds) / 3600; // Переводим в часы
        char sign = timeZoneSeconds > 0 ? '+' : '-'; // Определяем знак смещения +/-
        String timeZone = "GMT" + sign + hoursOffSet; // Сформируем строку вида "GMT+/-h"
        ZoneId zoneId = ZoneId.of(timeZone); // Переведем строку в ZoneId

        // Timestamp long -> Local date time
        Instant sunriseInstant = Instant.ofEpochSecond(sunrise);
        Instant sunsetInstant = Instant.ofEpochSecond(sunset);

        LocalDateTime sunriseTime = LocalDateTime.ofInstant(sunriseInstant, zoneId);
        LocalDateTime sunsetTime = LocalDateTime.ofInstant(sunsetInstant, zoneId);

        String text = """
                🌅 <i>Восход Солнца</i>: %s
                🌄 <i>Заход Солнца</i>: %s
                """;

        return String.format(text, sunriseTime.format(formatter), sunsetTime.format(formatter));
    }

    /**
     * Направление ветра
     * Из градусов в наименование направления
     */
    private String windDirectionFromDegrees(int degrees) {
        if (degrees >= 337.5 || degrees <= 22.5) {
            return """
                    ⬆️ Северный
                    """;
        } else if (degrees > 22.5 && degrees <= 67.5) {
            return """
                    ↗️ Северо-Восточный
                    """;
        } else if (degrees > 67.5 && degrees <= 112.5) {
            return """
                    ➡️ Восточный
                    """;
        } else if (degrees > 112.5 && degrees <= 157.5) {
            return """
                    ↘️ Юго-Восточный
                    """;
        } else if (degrees > 157.5 && degrees <= 202.5) {
            return """
                    ⬇️ Южный
                    """;
        } else if (degrees > 202.5 && degrees <= 247.5) {
            return """
                    ↙️ Юго-Западный
                    """;
        } else if (degrees > 247.5 && degrees <= 292.5) {
            return """
                    ⬅️ Западный
                    """;
        } else if (degrees > 292.5 && degrees <= 337.5) {
            return """
                    ↖️ Северо-Западный
                    """;
        } else return null;
    }


    /**
     * Ветер по шкале Бофорта
     *
     */
    private String windDescription(double speed) {
        String text;

        if (speed <= 0.3) {
            text = """
                    Штиль (%s м/с) 🌬️
                    """;
        } else if (speed > 0.3 && speed <= 1.6) {
            text = """
                    Тихий (%s м/с) 🌬️
                    """;
        } else if (speed > 1.6 && speed <= 3.4) {
            text = """
                     Лёгкий (%s м/с) 🌬️
                    """;
        } else if (speed > 3.4 && speed <= 5.5) {
            text = """
                    Слабый (%s м/с) 🌬️
                    """;
        } else if (speed > 5.5 && speed <= 8.0) {
            text = """
                    Умеренный (%s м/с) 🌬️
                    """;
        } else if (speed > 8.0 && speed <= 10.8) {
            text = """
                    Свежий (%s м/с) 🌬️
                    """;
        } else if (speed > 10.8 && speed <= 13.9) {
            text = """
                    Сильный (%s м/с) 💨
                    """;
        } else if (speed > 13.9 && speed <= 17.2) {
            text = """
                    Крепкий (%s м/с) 💨⚡️
                    """;
        } else if (speed > 17.2 && speed <= 20.7) {
            text = """
                    Очень крепкий (%s м/с) 💨⚡️
                    """;
        } else if (speed > 20.7 && speed <= 24.5) {
            text = """
                    Шторм (%s м/с) 🌩️⚡️
                    """;
        } else if (speed > 24.5 && speed <= 28.5) {
            text = """
                    Сильный шторм (%s м/с) 🌩️⚡️
                    """;
        } else if (speed > 28.5 && speed <= 32.7) {
            text = """
                    Жестокий шторм (%s м/с) 🌪️⚡️
                    """;
        } else if (speed > 32.7) {
            text = """
                    Ураган (%s м/с) 🌪️⚡️
                    """;
        } else text = """
                💨 %s м/с
                """;

        return String.format(text, speed);
    }


}
