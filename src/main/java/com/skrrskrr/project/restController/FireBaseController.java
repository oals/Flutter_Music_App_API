package com.skrrskrr.project.restController;

import com.google.firebase.auth.FirebaseAuthException;
import com.skrrskrr.project.dto.AuthResponseDto;
import com.skrrskrr.project.service.FireBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class FireBaseController {

    private final FireBaseService fireBaseService;

    /**
     * idToken 검사
     * @param token
     * @return
     */
    @PostMapping("/auth/fireBaseAuthing")
    public ResponseEntity<AuthResponseDto> fireBaseAuthing(@RequestHeader("Authorization") String token) throws FirebaseAuthException {

        String idToken = token.replace("Bearer ", "");

        fireBaseService.verifyFbToken(idToken);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
