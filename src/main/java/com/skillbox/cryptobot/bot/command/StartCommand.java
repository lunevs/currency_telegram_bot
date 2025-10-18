package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscription;
import com.skillbox.cryptobot.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * Обработка команды начала работы с ботом
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StartCommand implements IBotCommand {

    private final SubscriptionService subscriptionService;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long telegramUserId = message.getFrom().getId();
        String answerText = """
                Привет! Данный бот помогает отслеживать стоимость биткоина.
                Поддерживаемые команды:
                 /subscribe [число] - подписаться на стоимость биткоина в USD
                 /get_price - получить стоимость биткоина
                 /get_subscription - получить текущую подписку
                 /unsubscribe - отменить подписку на стоимость
                """;
        SendMessage answer = SendMessage.builder()
                .chatId(telegramUserId.toString())
                .text(answerText)
                .build();
        subscriptionService.getSubscriptionByTelegramUserId(telegramUserId)
                .orElseGet(() -> subscriptionService.createNewSubscription(telegramUserId));

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}