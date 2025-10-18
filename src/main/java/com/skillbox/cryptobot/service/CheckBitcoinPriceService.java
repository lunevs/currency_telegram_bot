package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class CheckBitcoinPriceService {

    @Value("${telegram.bot.check.enabled}")
    private boolean checkEnabled;

    private final CryptoCurrencyService service;
    private final SubscriptionService subscriptionService;
    private final TelegramNotificationService notificationService;

    @Scheduled(fixedRateString = "${telegram.bot.check.interval}")
    public void checkBitcoinPrice(){
        if (!checkEnabled) {
            return;
        }
        Double btcPrice = service.getBitcoinPrice();
        List<Subscription> subscriptions = subscriptionService.findSubscriptionsWithPriceGreaterThan(btcPrice);
        notificationService.notifyToBuy(subscriptions);

    }

}
