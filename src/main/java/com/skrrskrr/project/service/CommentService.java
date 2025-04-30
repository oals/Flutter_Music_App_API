package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.CommentResponseDto;
import com.skrrskrr.project.dto.TrackRequestDto;

import java.util.HashMap; import java.util.Map;

public interface CommentService {


    CommentResponseDto setComment(CommentRequestDto commentRequestDto);

    void setCommentLike(CommentRequestDto commentRequestDto);

    CommentResponseDto getComment(CommentRequestDto commentRequestDto);

}
