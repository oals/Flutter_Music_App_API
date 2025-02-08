package com.skrrskrr.project.restController;

import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationsController {

    final private NotificationsService notificationsService;



    @GetMapping("/api/getNotifications")
    public HashMap<String,Object> getNotifications(@RequestParam Long memberId,@RequestParam Long listIndex){
        log.info("getNotifications");
        return notificationsService.getNotifications(memberId,listIndex);
    }


    @PostMapping("/api/setNotificationIsView")
    public HashMap<String,Object> setNotificationIsView(@RequestBody HashMap<String,Long> hashMap){
        log.info("setNotificationIsView");
        return notificationsService.setNotificationIsView(hashMap.get("notificationId"),hashMap.get("memberId"));
    }

    @PostMapping("/api/setAllNotificationisView")
    public HashMap<String,Object> setAllNotificationisView(@RequestBody HashMap<String,Long> hashMap){
        log.info("setAllNotificationisView");
        return notificationsService.setAllNotificationisView(hashMap.get("memberId"));


    }
    @PostMapping("/api/setDelNotificationIsView")
    public HashMap<String,Object> setDelNotificationIsView(@RequestBody HashMap<String,Long> hashMap){
        log.info("setDelNotificationIsView");
        return notificationsService.setDelNotificationIsView(hashMap.get("memberId"));
    }
}
