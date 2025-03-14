package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;


    @PostMapping(value = "/api/setComment")
    public Map<String, Object> setComment(@RequestBody CommentRequestDto commentRequestDto) {

        log.info("setComment");
        // 댓글 추가 서비스 호출
        return commentService.setComment(commentRequestDto);
    }

    @PostMapping(value = "/api/setCommentLike")
    public Map<String, Object> setCommentLike(@RequestBody CommentRequestDto commentRequestDto) {

        return commentService.setCommentLike(commentRequestDto);
    }

    @GetMapping(value = "/api/getComment")
    public Map<String, Object> getComment(CommentRequestDto commentRequestDto) {

        log.info("getComment");
        return commentService.getComment(commentRequestDto);
    }


    @GetMapping(value = "/api/getChildComment")
    public Map<String, Object> getChildComment(CommentRequestDto commentRequestDto) {

        log.info("getChildComment");
        return commentService.getChildComment(commentRequestDto);
    }

}
