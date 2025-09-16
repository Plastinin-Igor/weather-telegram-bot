package ru.plastinin.weather_telegram_bot.bot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.plastinin.weather_telegram_bot.exception.ServiceException;
import ru.plastinin.weather_telegram_bot.service.WeatherService;

@Component
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
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case MOSCOW -> getMoscowWeather(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void startCommand(Long chatId, String userName) {
        String text = """
                Добро пожаловать в бот, %s!
                
                Здесь вы сможете узнать текущую погоду.
                
                Для этого воспользуйтесь командами:
                /moscow - погода в Москве
                
                Дополнительные команды:
                /help
                """;
        String formatedText = String.format(text, userName);
        sendMessage(chatId, formatedText);
    }

    private void getMoscowWeather(Long chatId) {
        try {
            String data = service.getWeather("Moscow,ru");
            sendMessage(chatId, data);
        } catch (ServiceException e) {
            LOG.error("Error: {}", e.getMessage()); //TODO сформулировать нормально сообщение об ошибке
        }
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }


}
