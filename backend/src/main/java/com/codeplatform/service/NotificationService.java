package com.codeplatform.service;

import com.codeplatform.common.Mapper;
import com.codeplatform.dto.NotificationResponse;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Mapper mapper;

    public PageResponse<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Page<com.codeplatform.entity.Notification> page =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(page, mapper::toNotificationResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
