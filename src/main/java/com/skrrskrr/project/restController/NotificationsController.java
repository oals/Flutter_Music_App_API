package com.skrrskrr.project.restController;

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
    public Map<String,Object> getNotifications(@RequestParam Long memberId,@RequestParam Long listIndex){
        log.info("getNotifications");
        return notificationsService.getNotifications(memberId,listIndex);
    }


    @PostMapping("/api/setNotificationIsView")
    public Map<String,Object> setNotificationIsView(@RequestBody Map<String,Long> hashMap){
        log.info("setNotificationIsView");
        return notificationsService.setNotificationIsView(hashMap.get("notificationId"),hashMap.get("memberId"));
    }

    @PostMapping("/api/setAllNotificationisView")
    public Map<String,Object> setAllNotificationisView(@RequestBody Map<String,Long> hashMap){
        log.info("setAllNotificationisView");
        return notificationsService.setAllNotificationisView(hashMap.get("memberId"));


    }
    @PostMapping("/api/setDelNotificationIsView")
    public Map<String,Object> setDelNotificationIsView(@RequestBody Map<String,Long> hashMap){
        log.info("setDelNotificationIsView");
        return notificationsService.setDelNotificationIsView(hashMap.get("memberId"));
    }
}
