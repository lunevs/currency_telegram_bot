package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.SubscriptionService;
import com.skillbox.cryptobot.utils.PriceValidator;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final SubscriptionService subscriptionService;
    private final CryptoCurrencyService service;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long userId = message.getFrom().getId();
        try {
            processSubscription(arguments, absSender, userId);
        } catch (Exception e) {
            log.error("Error occurred in /subscribe command", e);
        }
    }

    private void processSubscription(String[] arguments, AbsSender absSender, Long userId) throws TelegramApiException {
        Optional<Double> subscriptionPrice = getPriceFromArguments(arguments);
        if (subscriptionPrice.isPresent()) {
            subscriptionService.updateSubscriptionPrice(userId, subscriptionPrice.get());
            sendSubscriptionInfoMessage(
                    absSender, userId.toString(),
                    MessageFormat.format("Новая подписка создана на стоимость {0}", subscriptionPrice.get()));
            sendBtcPriceMessage(absSender, userId.toString());
        } else {
            sendSubscriptionInfoMessage(
                    absSender, userId.toString(),
                    "Необходимо указать стоимость. Формат команды: /subscribe [стоимость]");
        }

    }

    private Optional<Double> getPriceFromArguments(String[] arguments) {
        if (arguments == null || arguments.length == 0 || !PriceValidator.isPositiveDouble(arguments[0])) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(arguments[0]));
    }

    private void sendSubscriptionInfoMessage(AbsSender absSender, String chatId, String infoMessage) throws TelegramApiException {
        SendMessage subscribeAnswer = SendMessage.builder()
                .chatId(chatId)
                .text(infoMessage)
                .build();
        absSender.execute(subscribeAnswer);
    }

    private void sendBtcPriceMessage(AbsSender absSender, String chatId) throws TelegramApiException {
        SendMessage priceAnswer = SendMessage.builder()
                .chatId(chatId)
                .text("Текущая цена биткоина " + TextUtil.toString(service.getBitcoinPrice()) + " USD")
                .build();
        absSender.execute(priceAnswer);
    }
}