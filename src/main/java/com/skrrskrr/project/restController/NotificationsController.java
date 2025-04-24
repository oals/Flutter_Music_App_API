package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationsController {

    final private NotificationsService notificationsService;

    @GetMapping("/api/getNotifications")
    public ResponseEntity<NotificationResponseDto> getNotifications(NotificationsRequestDto notificationsRequestDto){
        log.info("getNotifications");
        NotificationResponseDto notificationResponseDto = notificationsService.getNotifications(notificationsRequestDto);
        return ResponseEntity.ok(notificationResponseDto);
    }

    @PostMapping("/api/setNotificationIsView")
    public ResponseEntity<NotificationResponseDto> setNotificationIsView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setNotificationIsView");
        NotificationResponseDto notificationResponseDto = notificationsService.setNotificationIsView(notificationsRequestDto);
        return ResponseEntity.ok(notificationResponseDto);
    }

    @PostMapping("/api/setAllNotificationIsView")
    public ResponseEntity<Void> setAllNotificationIsView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setAllNotificationIsView");
        notificationsService.setAllNotificationIsView(notificationsRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/setDelNotificationIsView")
    public ResponseEntity<Void> setDelNotificationIsView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setDelNotificationIsView");
        notificationsService.setDelNotificationIsView(notificationsRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
