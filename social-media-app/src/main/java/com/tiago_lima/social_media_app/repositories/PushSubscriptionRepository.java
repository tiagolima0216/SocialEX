package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.domain.entities.PushSubscription;
import com.tiago_lima.social_media_app.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {
    List<PushSubscription> findByUser(User recipient);
}
