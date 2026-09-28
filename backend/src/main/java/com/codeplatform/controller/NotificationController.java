package com.codeplatform.controller;

import com.codeplatform.common.SecurityUtils;
import com.codeplatform.dto.NotificationResponse;
import com.codeplatform.dto.PageResponse;
import com.codeplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return notificationService.getNotifications(SecurityUtils.requireUserId(), pageable);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.getUnreadCount(SecurityUtils.requireUserId()));
    }

    @PostMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllAsRead(SecurityUtils.requireUserId());
    }
}
