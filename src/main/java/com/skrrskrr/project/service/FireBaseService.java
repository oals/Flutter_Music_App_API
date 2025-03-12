package com.skrrskrr.project.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.skrrskrr.project.dto.FcmSendDTO;

public interface FireBaseService {

    FirebaseToken verifyFbToken(String idToken) throws FirebaseAuthException;

    void sendPushNotification(FcmSendDTO fcmSendDTO) throws Exception;
}
