package ru.plastinin.weather_telegram_bot.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.plastinin.weather_telegram_bot.bot.WeatherTelegramBot;

@Configuration
public class WeatherTelegramBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(WeatherTelegramBot weatherTelegramBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(weatherTelegramBot);
        return api;
    }


    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

}
