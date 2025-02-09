package com.skrrskrr.project.restController;

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
    public Map<String, Object> setComment(@RequestBody Map<String, Object> hashMap) {

        log.info("setComment");

        Long trackId = Long.valueOf(hashMap.get("trackId").toString());
        Long memberId = Long.valueOf(hashMap.get("memberId").toString());
        String commentText = hashMap.get("commentText").toString();
        Long commentId = 0L;

        if(hashMap.get("commentId") != null){
            commentId = Long.valueOf(hashMap.get("commentId").toString());
        } else {
            commentId = null;
        }

        // 댓글 추가 서비스 호출
        return commentService.setComment(trackId, memberId, commentText,commentId);
    }

    @PostMapping(value = "/api/setCommentLike")
    public Map<String, Object> setCommentLike(@RequestBody Map<String, Object> hashMap) {
        Long commentId = Long.valueOf(hashMap.get("commentId").toString());
        Long memberId = Long.valueOf(hashMap.get("memberId").toString());
        return commentService.setCommentLike(commentId, memberId);
    }

    @GetMapping(value = "/api/getComment")
    public Map<String, Object> getComment(@RequestParam Long trackId, @RequestParam Long memberId) {

        log.info("getComment");
        return commentService.getComment(trackId, memberId);
    }


    @GetMapping(value = "/api/getChildComment")
    public Map<String, Object> getChildComment(@RequestParam Long commentId, @RequestParam Long memberId) {

        log.info("getChildComment");
        return commentService.getChildComment(commentId, memberId);
    }

}
