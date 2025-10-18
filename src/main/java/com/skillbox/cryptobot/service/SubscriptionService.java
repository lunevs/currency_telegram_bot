package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.model.Subscription;
import com.skillbox.cryptobot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Optional<Subscription> getSubscriptionByTelegramUserId(Long telegramUserId) {
        log.info("Get subscription by telegram user id: {}", telegramUserId);
        return subscriptionRepository.findByTelegramUserId(telegramUserId);
    }

    @Transactional
    public Subscription save(Subscription subscription) {
        log.info("Saving subscription for user: {}", subscription.getTelegramUserId());

        return subscriptionRepository.findByTelegramUserId(subscription.getTelegramUserId())
                .map(existing -> updateExistingSubscription(existing, subscription))
                .orElseGet(() -> createNewSubscription(subscription.getTelegramUserId()));
    }

    public boolean existsByTelegramUserId(Long telegramUserId) {
        return getSubscriptionByTelegramUserId(telegramUserId)
                .map(subscription -> subscription.getPrice() != null)
                .orElse(false);
    }

    @Transactional
    public void updateSubscriptionPrice(Long telegramUserId, Double price ) {
        Subscription subscription = getSubscriptionByTelegramUserId(telegramUserId)
                .orElseGet(() -> createNewSubscription(telegramUserId));
        subscription.setPrice(price);
        subscriptionRepository.save(subscription);
    }

    public Optional<Double> findPriceByTelegramUserId(Long telegramUserId) {
        return subscriptionRepository.findByTelegramUserId(telegramUserId)
                .map(Subscription::getPrice);
    }

    @Transactional
    public boolean deleteSubscription(Long telegramUserId) {
        log.info("Deleting subscription for user: {}", telegramUserId);

        return subscriptionRepository.findByTelegramUserId(telegramUserId)
                .filter(s -> s.getPrice() != null)
                .map(s -> {
                    s.setPrice(null);
                    subscriptionRepository.save(s);
                    return true;
                })
                .orElse(false);
    }

    public List<Subscription> findSubscriptionsWithPriceGreaterThan(Double btcPrice) {
        log.debug("Finding subscriptions with price above: {}", btcPrice);
        return subscriptionRepository.findAllByPriceGreaterThan(btcPrice);
    }

    public void updateNotificationTime(Set<Long> notificationIdsList) {
        if (notificationIdsList == null || notificationIdsList.isEmpty()) {
            log.debug("No user IDs provided for notification time update");
            return;
        }

        log.info("Updating notification time for {} users", notificationIdsList.size());
        subscriptionRepository.updateNotificationTimeByTelegramUserIds(LocalDateTime.now(), notificationIdsList);
    }

    public Subscription createNewSubscription(Long telegramUserId) {
        log.info("Creating new subscription for user: {}", telegramUserId);
        Subscription subscription = Subscription.create(null, telegramUserId);
        return subscriptionRepository.save(subscription);
    }

    private Subscription updateExistingSubscription(Subscription existingSubscription, Subscription newSubscription) {
        log.info("Updating existing subscription for user: {}", existingSubscription.getTelegramUserId());
        if (newSubscription.getPrice() != null) {
            existingSubscription.setPrice(newSubscription.getPrice());
        }
        return subscriptionRepository.save(existingSubscription);
    }



}
