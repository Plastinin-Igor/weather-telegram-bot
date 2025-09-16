package ru.plastinin.weather_telegram_bot.client;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;

import java.io.IOException;
import java.util.Optional;

@Component
public class WeatherClient {

    @Autowired
    private OkHttpClient client;

    @Value("${openweathermap.url}")
    private String openweathermapUrl;

    @Value("${openweathermap.apiKeys}")
    private String apiKeys;

    public Optional<String> getOpenWeatherMapData(String cityName) throws ServiceException {
        Request request = new Request.Builder()
                .url(urlBuilder(cityName))
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения данных о погоде", e);
        }
    }


    //TODO список параметров необходимо расширить.

    // Сборка URL по параметрам
    private HttpUrl urlBuilder(String cityName) {
        HttpUrl baseUrl = HttpUrl.parse(openweathermapUrl);
        if (baseUrl != null) {
            return baseUrl.newBuilder()
                    .addQueryParameter("q", cityName)
                    .addQueryParameter("APPID", apiKeys)
                    .addQueryParameter("units", "metric")
                    .build();
        } else {
            throw new IllegalArgumentException("Ошибка добавления параметра: " + cityName + " в url.");
        }
    }

}
