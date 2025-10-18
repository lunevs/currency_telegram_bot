package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByTelegramUserId(Long telegramUserId);

    List<Subscription> findAllByPriceGreaterThan(Double price);

    @Modifying
    @Transactional
    @Query("update Subscription s set s.lastNotificationTime = :time where s.telegramUserId in :ids")
    void updateNotificationTimeByTelegramUserIds(@Param("time") LocalDateTime time, @Param("ids") Collection<Long> userIdsList);
}
