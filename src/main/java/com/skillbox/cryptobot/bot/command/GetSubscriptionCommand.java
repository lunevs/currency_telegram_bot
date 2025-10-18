package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final SubscriptionService subscriptionService;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long userId = message.getFrom().getId();
        String replyMessage = subscriptionService.findPriceByTelegramUserId(userId)
                .map(s -> MessageFormat.format("Вы подписаны на стоимость биткоина {0} USD", s))
                .orElse("Активные подписки отсутствуют");
        SendMessage answer = SendMessage.builder()
                .chatId(userId.toString())
                .text(replyMessage)
                .build();

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_subscription command", e);
        }

    }
}