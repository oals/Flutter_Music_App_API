package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.CommentResponseDto;
import com.skrrskrr.project.dto.FollowRequestDto;
import com.skrrskrr.project.dto.FollowResponseDto;
import com.skrrskrr.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;


    @PostMapping(value = "/api/setComment")
    public ResponseEntity<Void> setComment(@RequestBody CommentRequestDto commentRequestDto) {
        log.info("setComment");
        commentService.setComment(commentRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(value = "/api/setCommentLike")
    public ResponseEntity<Void>setCommentLike(@RequestBody CommentRequestDto commentRequestDto) {
        log.info("setCommentLike");
        commentService.setCommentLike(commentRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/api/getComment")
    public ResponseEntity<CommentResponseDto> getComment(CommentRequestDto commentRequestDto) {
        log.info("getComment");
        CommentResponseDto commentResponseDto = commentService.getComment(commentRequestDto);
        return ResponseEntity.ok(commentResponseDto);
    }

    @GetMapping(value = "/api/getChildComment")
    public ResponseEntity<CommentResponseDto> getChildComment(CommentRequestDto commentRequestDto) {
        log.info("getChildComment");
        CommentResponseDto commentResponseDto = commentService.getChildComment(commentRequestDto);
        return ResponseEntity.ok(commentResponseDto);
    }
}
