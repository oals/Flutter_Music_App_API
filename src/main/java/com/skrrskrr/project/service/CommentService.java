package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.TrackRequestDto;

import java.util.HashMap; import java.util.Map;

public interface CommentService {


    Map<String,Object> setComment(CommentRequestDto commentRequestDto);

    Map<String,Object> setCommentLike(CommentRequestDto commentRequestDto);

    Map<String,Object> getComment(CommentRequestDto commentRequestDto);

    Map<String,Object> getChildComment(CommentRequestDto commentRequestDto);


}
