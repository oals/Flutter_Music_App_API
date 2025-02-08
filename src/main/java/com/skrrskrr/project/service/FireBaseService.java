package com.skrrskrr.project.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public interface FireBaseService {

    FirebaseToken verifyFbToken(String idToken) throws FirebaseAuthException;

    void sendPushNotification(Long memberId,
                              String title,
                              String body,
                              Long notificationType,
                              Long notificationTrackId,
                              Long notificationCommentId,
                              Long notificationMemberId
                              ) throws Exception;
}
