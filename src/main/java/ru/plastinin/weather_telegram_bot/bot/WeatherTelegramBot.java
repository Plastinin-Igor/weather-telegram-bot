package ru.plastinin.weather_telegram_bot.bot;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.service.WeatherService;

@Component
@Slf4j
public class WeatherTelegramBot extends TelegramLongPollingBot {

    @Autowired
    WeatherService service;

    private static final String START = "/start";
    private static final String MOSCOW = "/moscow";
    private static final String HELP = "/help";

    private static final Logger LOG = LoggerFactory.getLogger(WeatherTelegramBot.class);

    @Value("${bot.username}")
    private String botUsername;

    public WeatherTelegramBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {

        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        Message msg = update.getMessage();

        if (msg.hasLocation()) {
            // Получение данных по геопозиции
            Location location = msg.getLocation();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            getWeather(chatId, latitude, longitude);
            log.info("Geo Location from username: {}, chatId: {}.", userName, chatId);
        } else {
            // Обработка текстовых сообщений
            if (!update.hasMessage() || !msg.hasText()) {
                return;
            }
            switch (message) {
                case START -> {
                    startCommand(chatId, userName);
                    log.info("START from username: {}, chatId: {}.", userName, chatId);
                }
                case HELP -> {
                    helpCommand(chatId, userName);
                    log.info("HELP from username: {}, chatId: {}.", userName, chatId);
                }
                default -> {
                    sendMessage(chatId, "Команда не поддерживается");
                    log.info("Команда не поддерживается username: {}, chatId: {}.", userName, chatId);
                }
            }
        }

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void startCommand(Long chatId, String userName) {
        String text = """
                ☁️ 🌦️ 🌧️ 🌞 🌩️ ⛅ ❄️ 🌡️ 🌫️ ✨
                %s, добро пожаловать в бот!
                Здесь вы сможете узнать текущую погоду по геолокации
                
                Для получения информации о погоде отправьте точку Геопозиции (геолокация 📍 «Location»).
                
                Команды:
                Начало работы 🚀 /start
                Справка 🔍 /help
                """;
        String formatedText = String.format(text, userName);
        sendMessage(chatId, formatedText);
    }

    private void helpCommand(Long chatId, String userName) {
        String text = """
                ️ ☁️ 🌦️ 🌧️ 🌞 🌩️ ⛅ ❄️ 🌡️ 🌫️ ✨
                Телеграм-бот показывает информацию о текущей погоде по местоположению
                
                Доступна следующая информация:
                
                 - Температура 🌡️ °C
                 - Ощущается как 🤔 °C
                 - Минимальная температура 📉 °C
                 - Максимальная температура 📈️ °C
                 - Атмосферное Давление 🌐 кПа
                 - Влажность воздуха 💧 %
                
                
                Для получения информации:
                
                1. Нажмите на поле ввода сообщения внизу экрана 🖥️.
                2. Выберите значок скрепки 📎.
                3. Найдите пункт "Геопозиция" 📌 или «Местоположение» 📍.
                4. Разрешите приложению доступ к вашей геолокации, если потребуется ✅.
                5. После появления вашего текущего положения нажмите кнопку отправки "Отправить выбранную геопозицию" 🚀.
                6. В ответ на вашу геопозицию придет сообщение, в котором будет краткая погодная сводка 📝📊 .
                
                Команды:
                Начало работы 🚀 /start
                Справка 🔍 /help
                
                
                Источником данных является сервис OpenWeatherMap org
                
                OpenWeatherMap - один из популярных API сервисов для получения информации о погоде в реальном времени.
                Подробнее о сервисе смотрите на официальном сайте 🌐 https://openweathermap.org/.
                
                """;
        sendMessage(chatId, text);
    }

    private void getWeather(Long chatId, double lat, double lon) {
        try {
            String data = service.getWeather(lat, lon);
            sendMessage(chatId, data);
        } catch (ServiceException e) {
            LOG.error("Error: {}", e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        sendMessage.setParseMode("HTML");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

}
