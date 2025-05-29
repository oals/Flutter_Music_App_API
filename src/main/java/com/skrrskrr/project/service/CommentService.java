package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.CommentResponseDto;

public interface CommentService {

    CommentResponseDto setComment(CommentRequestDto commentRequestDto);

    void setCommentLike(CommentRequestDto commentRequestDto);

    CommentResponseDto getComment(CommentRequestDto commentRequestDto);

}
