package com.skrrskrr.project.service;

import java.util.HashMap;

public interface CommentService {


    HashMap<String,Object> setComment(Long trackId, Long memberId,
                                      String commentText, Long commentId);

    HashMap<String,Object> setCommentLike(Long commentId, Long memberId);

    HashMap<String,Object> getComment(Long trackId, Long memberId);

    HashMap<String,Object> getChildComment(Long commentId, Long memberId);


}
