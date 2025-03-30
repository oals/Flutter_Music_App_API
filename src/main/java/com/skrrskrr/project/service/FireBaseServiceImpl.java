package com.skrrskrr.project.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FcmSendDto;
import com.skrrskrr.project.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Log4j2
public class FireBaseServiceImpl implements FireBaseService {


    @PersistenceContext
    EntityManager em;

    // Firebase 클라우드 메시징에 사용할 서비스 계정 키 파일 경로
    @Value("${SERVICE_ACCOUNT_JSON_PATH}")
    private String SERVICE_ACCOUNT_JSON_PATH;

    // Firebase 클라우드 메시징 API URL
    @Value("${FCM_API_URL}")
    private String FCM_API_URL;


    /**
     *  aUth-Token 유효성 검사
     */
    public FirebaseToken verifyFbToken(String idToken) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }


    /**
     * 댓글 작성 시 push 알림
     */
    public void sendPushNotification(FcmSendDto fcmSendDto) throws Exception {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        // 디바이스 토큰 가져오기
        Member member = jpaQueryFactory
                .selectFrom(qMember)
                .where(qMember.memberId.eq(fcmSendDto.getMemberId()))
                .fetchOne();

        assert member != null;

        saveNotifications(fcmSendDto, member);

        sendNotificationSetFcm(fcmSendDto, member);

    }

    private void sendNotificationSetFcm(FcmSendDto fcmSendDto, Member member) throws Exception {

        String deviceToken = member.getMemberDeviceToken();

        // 1. OAuth2 인증 토큰 얻기
        String accessToken = getAccessToken();

        // 3. 알림 메시지 구성
        JSONObject message = new JSONObject();
        JSONObject notification = new JSONObject();

        // 알림 제목과 본문 설정
        notification.put("title", fcmSendDto.getTitle());  // 알림 제목
        notification.put("body", fcmSendDto.getBody());    // 알림 본문

        // 메시지에 notification 객체와 token 필드 추가
        JSONObject messageBody = new JSONObject();
        messageBody.put("token", deviceToken);  // 수신할 디바이스의 토큰 (to 대신 token 사용)
        messageBody.put("notification", notification);  // 알림 내용 추가

        // message 객체에 messageBody 추가
        message.put("message", messageBody);

        // 4. FCM으로 메시지 전송
        sendNotificationToFCM(message.toString(), accessToken);
    }


    private void saveNotifications(FcmSendDto fcmSendDto, Member member){
        Notifications notifications = Notifications.builder()
                .member(member)
                .notificationMsg(fcmSendDto.getBody())
                .notificationType(fcmSendDto.getNotificationType())
                .notificationTrackId(fcmSendDto.getNotificationTrackId())
                .notificationCommentId(fcmSendDto.getNotificationCommentId())
                .notificationMemberId(fcmSendDto.getNotificationMemberId())
                .notificationIsView(false)
                .notificationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();

        em.persist(notifications);
    }

    /**
     * Firebase Cloud Messaging 액세스 토큰을 얻는 메소드
     */
    private String getAccessToken() throws IOException {
        // GoogleCredentials를 사용하여 서비스 계정으로부터 액세스 토큰을 얻습니다.
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(SERVICE_ACCOUNT_JSON_PATH);
        if (serviceAccount == null) {
            throw new FileNotFoundException("Service account file not found in resources");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

        AccessToken accessToken = null;
        try {
            // 만약 자격 증명이 만료되었으면 새로 갱신
            credentials.refreshIfExpired();

            // 액세스 토큰을 가져옵니다
            accessToken = credentials.getAccessToken();
            String tokenValue = accessToken.getTokenValue();
            System.out.println("Successfully retrieved access token: " + tokenValue);  // 로그 추가

            return tokenValue;
        } catch (IOException e) {
            System.err.println("Error while refreshing or fetching the access token: " + e.getMessage());
        }

        return accessToken.getTokenValue();  // 액세스 토큰 값을 반환합니다.
    }



    /**
     * FCM API로 푸시 알림 메시지를 보내는 메소드
     */
    private void sendNotificationToFCM(String messageContent, String accessToken) throws Exception {

        // FCM API 요청을 위한 연결 설정
        HttpURLConnection connection = (HttpURLConnection) new URL(FCM_API_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 메시지 본문을 서버로 전송
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(messageContent.getBytes("UTF-8"));
            outputStream.flush();
        }

        // 서버 응답 확인
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {  // 200 OK 응답
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("FCM Response: " + response.toString());
            }
        } else {  // 오류가 발생했을 경우
            // 오류 응답 읽기
            System.err.println("Error: " + responseCode);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("FCM Error Response: " + errorResponse.toString());
            }
        }
    }

}
