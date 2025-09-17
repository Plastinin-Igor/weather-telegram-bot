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


    public Optional<String> getOpenWeatherMapData(double lat, double lon) throws ServiceException {
        Request request = new Request.Builder()
                .url(urlBuilder(lat, lon))
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения данных о погоде", e);
        }
    }


    // Сборка URL по параметрам
    private HttpUrl urlBuilder(double lat, double lon) {
        HttpUrl baseUrl = HttpUrl.parse(openweathermapUrl);
        if (baseUrl != null) {
            return baseUrl.newBuilder()
                    .addQueryParameter("lat", String.valueOf(lat))
                    .addQueryParameter("lon", String.valueOf(lon))
                    .addQueryParameter("appid", apiKeys)
                    .addQueryParameter("units", "metric")
                    .addQueryParameter("lang", "ru")
                    .build();
        } else {
            throw new IllegalArgumentException("Error adding parameters to URL");
        }
    }

}
