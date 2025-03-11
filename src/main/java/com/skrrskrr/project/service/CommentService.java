package com.skrrskrr.project.service;

import java.util.HashMap; import java.util.Map;

public interface CommentService {


    Map<String,Object> setComment(Long trackId, Long memberId,
                                      String commentText, Long commentId);

    Map<String,Object> setCommentLike(Long commentId, Long memberId);

    Map<String,Object> getComment(Long trackId, Long memberId);

    Map<String,Object> getChildComment(Long commentId, Long memberId);

    Long getTrackCommentCnt(Long memberId, Long trackId);


}
