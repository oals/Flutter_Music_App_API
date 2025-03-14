package com.skrrskrr.project.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.skrrskrr.project.dto.FcmSendDto;

public interface FireBaseService {

    FirebaseToken verifyFbToken(String idToken) throws FirebaseAuthException;

    void sendPushNotification(FcmSendDto fcmSendDTO) throws Exception;
}
