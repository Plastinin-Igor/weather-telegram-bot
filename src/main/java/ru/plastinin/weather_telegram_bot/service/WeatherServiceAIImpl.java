package ru.plastinin.weather_telegram_bot.service;

import chat.giga.model.completion.Choice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.plastinin.weather_telegram_bot.client.GigaChatClientForBot;
import ru.plastinin.weather_telegram_bot.client.WeatherClient;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;

import java.util.Optional;

@Service
public class WeatherServiceAIImpl implements WeatherServiceAI {

    @Autowired
    private WeatherClient client;

    @Autowired
    private GigaChatClientForBot gigaChatClient;

    @Override
    public String getWeather(double lat, double lon) throws ServiceException {
        Optional<String> jsonOptional = client.getOpenWeatherMapData(lat, lon);
        String jsonString = jsonOptional.orElseThrow(() -> new ServiceException("Unable to retrieve data " +
                                                                                "from weather service Openweathermap."));
        // Формируем prompt для GigaChat
        final String prompt = """
                %s
                Это данные с сервиса погоды OpenWeather. Проанализируйте их.
                Составьте краткую справку о погоде для телеграм бота.
                В тексте можно использовать немного эмодзи, которые соответствуют погоде.
                В тексте не используй наименование тегов, и разметку markdown.
                
                """.formatted(jsonString);

        final Choice choice = gigaChatClient.sendRequest(prompt).choices().get(0); // Берём первый выбор
        return choice.message().content();
    }

}
