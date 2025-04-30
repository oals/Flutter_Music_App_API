package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.CommentDto;
import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.CommentResponseDto;
import com.skrrskrr.project.dto.FcmSendDto;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.CommentSelectQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CommentServiceImpl implements CommentService {


    @PersistenceContext
    EntityManager entityManager;

    private final FireBaseService fireBaseService;
    private final JPAQueryFactory jpaQueryFactory;
    private final MemberService memberService;

    @Override
    public CommentResponseDto setComment(CommentRequestDto commentRequestDto) {

        CommentSelectQueryBuilder commentSelectQueryBuilder = new CommentSelectQueryBuilder(jpaQueryFactory);
        QTrack qTrack = QTrack.track;

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);
        Long fcmMsgRecvMemberId = null;

        Member member = memberService.getMemberEntity(commentRequestDto.getLoginMemberId());

        Track track = jpaQueryFactory.selectFrom(qTrack)
                .where(qTrack.trackId.eq(commentRequestDto.getTrackId()))
                .fetchOne();

        if (track == null) {
            throw new IllegalStateException("track cannot be null.");
        }

        fcmMsgRecvMemberId = track.getMemberTrackList().get(0).getMember().getMemberId();

        Comment insertComment = new Comment();
        insertComment.setTrack(track);
        insertComment.setMember(member);
        insertComment.setCommentText(commentRequestDto.getCommentText());
        insertComment.setCommentDate(formattedDate);
        insertComment.setCommentLikeCnt(0L);
        insertComment.setChildComments(new ArrayList<>());

        /// 자식 댓글 저장하는 쿼리 였을 시
        if (commentRequestDto.getCommentId() != null) {

            Comment parentComment = (Comment) commentSelectQueryBuilder.selectFrom(QComment.comment)
                    .findCommentByCommentId(commentRequestDto.getCommentId())
                    .fetchOne(Comment.class);

            /// 부모댓글 작성자
            fcmMsgRecvMemberId = parentComment.getMember().getMemberId();

            parentComment.getChildComments().add(insertComment);

            insertComment.setParentComment(parentComment);
        }

        entityManager.persist(insertComment);

        try {
            if (Objects.equals(commentRequestDto.getLoginMemberId(), fcmMsgRecvMemberId)) {

                FcmSendDto fcmSendDTO = FcmSendDto.builder()
                        .title("알림")
                        .body(member.getMemberNickName() + ": " + commentRequestDto.getCommentText())
                        .notificationType(2L)
                        .notificationTrackId(commentRequestDto.getTrackId())
                        .notificationCommentId(insertComment.getCommentId())
                        .memberId(fcmMsgRecvMemberId)
                        .build();


                fireBaseService.sendPushNotification(fcmSendDTO);
            }


            return CommentResponseDto.builder()
                    .comment(commentModelMapper(insertComment,false))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void setCommentLike(CommentRequestDto commentRequestDto) {

        CommentSelectQueryBuilder commentSelectQueryBuilder = new CommentSelectQueryBuilder(jpaQueryFactory);

        QCommentLike qCommentLike = QCommentLike.commentLike;

        CommentLike commentLike = jpaQueryFactory.selectFrom(qCommentLike)
                .where(qCommentLike.comment.commentId.eq(commentRequestDto.getCommentId())
                        .and(qCommentLike.member.memberId.eq(commentRequestDto.getLoginMemberId())))
                .fetchFirst();

        Member member = Member.builder()
                .memberId(commentRequestDto.getLoginMemberId())
                .build();

        Comment comment = (Comment) commentSelectQueryBuilder.selectFrom(QComment.comment)
                .findCommentByCommentId(commentRequestDto.getCommentId())
                .fetchOne(Comment.class);

        if (commentLike != null) {

            Boolean commentLikeStatus = commentLike.getCommentLikeStatus();

            jpaQueryFactory.update(qCommentLike)
                    .set(qCommentLike.commentLikeStatus, !commentLikeStatus)
                    .where(qCommentLike.comment.commentId.eq(commentRequestDto.getCommentId())
                            .and(qCommentLike.member.memberId.eq(commentRequestDto.getLoginMemberId())))
                    .execute();

            Comment comment1 = commentLike.getComment();

            if (commentLikeStatus) {
                comment.setCommentLikeCnt(comment.getCommentLikeCnt() - 1);
            } else {
                comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
            }

            entityManager.merge(comment1);

        } else {

            CommentLike insertCommentLike = new CommentLike();
            insertCommentLike.setComment(comment);
            insertCommentLike.setCommentLikeStatus(true);
            insertCommentLike.setMember(member);
            entityManager.persist(insertCommentLike);

            comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
            entityManager.merge(comment);
        }

    }

    private List<CommentDto> getAllChildComments(Comment comment, Long loginMemberId) {
        List<CommentDto> childCommentDtoList = new ArrayList<>();

        for (Comment childComment : comment.getChildComments()) {
            Boolean isLikeComment = !childComment.getCommentLikeList().isEmpty() &&
                    isCommentLike(childComment, loginMemberId);

            CommentDto childCommentDto = commentModelMapper(childComment, isLikeComment);

            childCommentDtoList.add(childCommentDto);

            childCommentDtoList.addAll(getAllChildComments(childComment, loginMemberId));
        }

        childCommentDtoList = childCommentDtoList.stream()
                .sorted(Comparator.comparing(CommentDto::getCommentId))
                .toList();

        return childCommentDtoList;
    }

    @Override
    public CommentResponseDto getComment(CommentRequestDto commentRequestDto) {

        CommentSelectQueryBuilder commentQueryBuilder = new CommentSelectQueryBuilder(jpaQueryFactory);

        List<Comment> commentList = commentQueryBuilder.selectFrom(QComment.comment)
                .findCommentByTrackId(commentRequestDto.getTrackId())
                .findParentComment()
                .orderByCommentIdDesc()
                .fetch(Comment.class);

        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            Boolean isLikeComment = !comment.getCommentLikeList().isEmpty() &&
                    isCommentLike(comment, commentRequestDto.getLoginMemberId());

            CommentDto parentCommentDto = commentModelMapper(comment, isLikeComment);

            List<CommentDto> allChildComments = getAllChildComments(comment, commentRequestDto.getLoginMemberId());
            parentCommentDto.setChildComments(allChildComments);

            commentDtoList.add(parentCommentDto);
        }

        return CommentResponseDto.builder()
                .commentList(commentDtoList)
                .build();
    }


    private CommentDto commentModelMapper(Comment comment, Boolean isLikeComment) {
        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .commentText(comment.getCommentText())
                .commentDate(comment.getCommentDate())
                .commentLikeCnt(comment.getCommentLikeCnt())
                .commentLikeStatus(isLikeComment)
                .trackId(comment.getTrack().getTrackId())
                .memberId(comment.getMember().getMemberId())
                .memberNickName(comment.getMember().getMemberNickName())
                .memberImagePath(comment.getMember().getMemberImagePath())
                .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getCommentId())
                .build();
    }


    private Boolean isCommentLike(Comment comment, Long memberId) {
        return comment.getCommentLikeList().stream()
                .anyMatch(commentLike ->
                        commentLike.getMember().getMemberId().equals(memberId) &&
                                commentLike.getCommentLikeStatus());
    }

    private List<CommentDto> addAllChildComments(List<CommentDto> childCommentList,
                                                 Comment parentComment,
                                                 Long memberId) {
        // 현재 부모 댓글에 대한 자식 댓글들을 가져옴
        for (Comment childComment : parentComment.getChildComments()) {

            // 현재 자식 댓글이 좋아요 여부를 체크
            Boolean isLikeChildComment = false;
            if (!childComment.getCommentLikeList().isEmpty()) {
                isLikeChildComment = childComment.getCommentLikeList().stream()
                        .anyMatch(commentLike ->
                                commentLike.getMember().getMemberId().equals(memberId) &&
                                        commentLike.getCommentLikeStatus());
            }

            // 자식 댓글을 DTO로 변환
            CommentDto childCommentDto = CommentDto.builder()
                    .commentId(childComment.getCommentId())
                    .commentText(childComment.getCommentText())
                    .commentDate(childComment.getCommentDate())
                    .trackId(parentComment.getTrack().getTrackId())
                    .memberId(childComment.getMember().getMemberId())
                    .commentLikeStatus(isLikeChildComment)
                    .commentLikeCnt(childComment.getCommentLikeCnt())
                    .memberNickName(childComment.getMember().getMemberNickName())
                    .parentCommentMemberId(childComment.getParentComment().getMember().getMemberId())
                    .parentCommentMemberNickName("@" + childComment.getParentComment().getMember().getMemberNickName())
                    .memberImagePath(childComment.getMember().getMemberImagePath())
                    .parentCommentId(childComment.getParentComment().getCommentId())
                    .build();

            // 현재 자식 댓글을 리스트에 추가
            childCommentList.add(childCommentDto);

            // 자식 댓글이 또 자식 댓글을 가질 수 있으므로, 재귀적으로 호출
            addAllChildComments(childCommentList, childComment, memberId);
        }

        return childCommentList;
    }


}
