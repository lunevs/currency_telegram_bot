package com.skillbox.cryptobot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "telegram_user_id", unique = true, nullable = false)
    private Long telegramUserId;

    @Column(name = "price")
    private Double price;

    @Column(name = "last_notification_time")
    private LocalDateTime lastNotificationTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return price != null;
    }

    public static Subscription create(Double price, Long telegramUserId) {
        return Subscription.builder()
                .id(UUID.randomUUID())
                .price(price)
                .telegramUserId(telegramUserId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
