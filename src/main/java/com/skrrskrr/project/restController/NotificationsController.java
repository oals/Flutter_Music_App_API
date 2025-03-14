package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.NotificationsRequestDto;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationsController {

    final private NotificationsService notificationsService;



    @GetMapping("/api/getNotifications")
    public Map<String,Object> getNotifications(NotificationsRequestDto notificationsRequestDto){
        log.info("getNotifications");
        return notificationsService.getNotifications(notificationsRequestDto);
    }


    @PostMapping("/api/setNotificationIsView")
    public Map<String,Object> setNotificationIsView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setNotificationIsView");
        return notificationsService.setNotificationIsView(notificationsRequestDto);
    }

    @PostMapping("/api/setAllNotificationisView")
    public Map<String,Object> setAllNotificationisView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setAllNotificationisView");
        return notificationsService.setAllNotificationisView(notificationsRequestDto);


    }
    @PostMapping("/api/setDelNotificationIsView")
    public Map<String,Object> setDelNotificationIsView(@RequestBody NotificationsRequestDto notificationsRequestDto){
        log.info("setDelNotificationIsView");
        return notificationsService.setDelNotificationIsView(notificationsRequestDto);
    }
}
