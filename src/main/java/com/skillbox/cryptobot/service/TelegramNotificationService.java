package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.model.Subscription;
import com.skillbox.cryptobot.utils.TextUtil;
import com.skillbox.cryptobot.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramNotificationService {

    @Value("${telegram.bot.notify.rate-limit.retry-attempts}")
    private int rateLimitRetryAttempts;

    @Value("${telegram.bot.notify.rate-limit.retry-delay}")
    private int rateLimitRetryDelay;

    @Value("${telegram.bot.notify.rate-limit.send-delay}")
    private int rateLimitSendDelay;


    private final CryptoCurrencyService currencyService;
    private final AbsSender botSender;
    private final SubscriptionService subscriptionService;

    public void notifyToBuy(Collection<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        Set<Long> updatedIds = filterSubscriptionsToNotify(subscriptions);
        if (!updatedIds.isEmpty()) {
            broadcastNotifications(updatedIds, getNotificationMessage());
            subscriptionService.updateNotificationTime(updatedIds);
        }
    }

    private void broadcastNotifications(Set<Long> updatedIds, String notificationMessage) {
        updatedIds.forEach(id -> {
            sendNotificationWithRetry(id, notificationMessage);
            delayBetweenMessages();
        });
    }

    private void sendNotificationWithRetry(Long chatId, String notificationMessage) {
        for (int attempt = 0; attempt < rateLimitRetryAttempts; attempt++) {
            try {
                sendNotification(chatId.toString(), notificationMessage);
                return;
            } catch (TelegramApiException e) {
                if (e.getMessage().contains("Too Many Requests")) {
                    handleRateLimit(chatId, notificationMessage, attempt);
                } else if (e.getMessage().contains("Forbidden")) {
                    handleBlockedUser(chatId);
                    return;
                } else {
                    log.error("Failed to send notification to {} (attempt {}): {}", chatId, attempt, e.getMessage());
                }
            }
        }
    }

    private void sendNotification(String chatId, String notificationMessage) throws TelegramApiException {
        SendMessage messageToSend = SendMessage.builder()
                .chatId(chatId)
                .text(notificationMessage)
                .build();
        botSender.execute(messageToSend);
    }

    private Predicate<Subscription> filterSubscriptionsToNotify() {
        return s -> s.getLastNotificationTime() == null || s.getLastNotificationTime().isBefore(TimeUtil.getTimeToCompare());
    }

    private void handleRateLimit(Long chatId, String notificationMessage, int attempt) {
        log.warn("Rate limit for user {} (attempt {}), waiting...", chatId, attempt);
        try {
            Thread.sleep((long) rateLimitRetryDelay * attempt);
            sendNotificationWithRetry(chatId, notificationMessage);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleBlockedUser(Long chatId) {
        log.info("User {} blocked the bot, unsubscribe", chatId);
        subscriptionService.deleteSubscription(chatId);
    }

    private Set<Long> filterSubscriptionsToNotify(Collection<Subscription> subscriptions) {
        return subscriptions.stream()
                .filter(filterSubscriptionsToNotify())
                .map(Subscription::getTelegramUserId)
                .collect(Collectors.toSet());
    }

    private String getNotificationMessage() {
        return "Пора покупать, стоимость биткоина " + TextUtil.toString(currencyService.getBitcoinPrice());
    }

    private void delayBetweenMessages() {
        try {
            Thread.sleep(rateLimitSendDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Notification broadcasting interrupted", e);
        }
    }

}
