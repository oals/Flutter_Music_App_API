package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.CommentDTO;
import com.skrrskrr.project.dto.FcmSendDTO;
import com.skrrskrr.project.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CommentServiceImpl implements CommentService{


    @PersistenceContext
    EntityManager entityManager;

    private final FireBaseService fireBaseService;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<String, Object> setComment(Long trackId, Long memberId, String commentText, Long commentId) {
        Map<String, Object> hashMap = new HashMap<>();
        
        QTrack qTrack = QTrack.track;

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        Long fcmMsgRecvMemberId = null;

        try {
            Member member = Member.builder()
                    .memberId(memberId)
                    .build();

            Track track = jpaQueryFactory.selectFrom(qTrack)
                    .where(qTrack.trackId.eq(trackId))
                    .fetchOne();

            assert track != null;
            fcmMsgRecvMemberId = track.getMemberTrackList().get(0).getMember().getMemberId();

            Comment insertComment = new Comment();
            insertComment.setTrack(track);
            insertComment.setMember(member);
            insertComment.setCommentText(commentText);
            insertComment.setCommentDate(formattedDate);
            insertComment.setCommentLikeCnt(0L);
            insertComment.setChildComments(new ArrayList<>());

            /// 자식 댓글 저장하는 쿼리 였을 시
            if( commentId != null) {
                QComment qComment = QComment.comment;

                Comment parentComment = jpaQueryFactory.selectFrom(qComment)
                                    .where(qComment.commentId.eq(commentId))
                                        .fetchFirst();

                /// 부모댓글 작성자
                fcmMsgRecvMemberId = parentComment.getMember().getMemberId();

                parentComment.getChildComments().add(insertComment);

                insertComment.setParentComment(parentComment);
            }

            entityManager.persist(insertComment);

            try{
                if(!Objects.equals(memberId, fcmMsgRecvMemberId)){

                    FcmSendDTO fcmSendDTO = FcmSendDTO.builder()
                            .title("알림")
                            .body(commentText)
                            .notificationType(2L)
                            .notificationTrackId(trackId)
                            .memberId(fcmMsgRecvMemberId)
                            .build();


                    fireBaseService.sendPushNotification(fcmSendDTO);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            // 성공 시
            hashMap.put("status", "OK");
        } catch (Exception e) {
            // 실패 시
            e.printStackTrace();
            hashMap.put("status", "FAIL");
        }

        return hashMap; // 최종 결과 반환
    }


    @Override
    public Map<String, Object> setCommentLike(Long commentId, Long memberId) {

        QCommentLike qCommentLike = QCommentLike.commentLike;
        QComment qComment = QComment.comment;
        Map<String,Object> hashMap = new HashMap<>();

        try{
            CommentLike commentLike = jpaQueryFactory.selectFrom(qCommentLike)
                    .where(qCommentLike.comment.commentId.eq(commentId)
                            .and(qCommentLike.member.memberId.eq(memberId)))
                    .fetchFirst();

            Member member = Member.builder()
                    .memberId(memberId)
                    .build();

            Comment comment = jpaQueryFactory.selectFrom(qComment)
                    .where(qComment.commentId.eq(commentId))
                    .fetchFirst();


            /// update
            if (commentLike != null) {

                boolean commentLikeStatus = commentLike.isCommentLikeStatus();

                jpaQueryFactory.update(qCommentLike)
                        .set(qCommentLike.commentLikeStatus, !commentLikeStatus)
                        .where(qCommentLike.comment.commentId.eq(commentId)
                                .and(qCommentLike.member.memberId.eq(memberId)))
                        .execute();

                Comment comment1 = commentLike.getComment();
                if(commentLikeStatus){
                    comment.setCommentLikeCnt(comment.getCommentLikeCnt() - 1);
                } else {
                    comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
                }

                entityManager.merge(comment1);

            } else {
                /// insert

                CommentLike insertCommentLike = new CommentLike();
                insertCommentLike.setComment(comment);
                insertCommentLike.setCommentLikeStatus(true);
                insertCommentLike.setMember(member);

                entityManager.persist(insertCommentLike);


                comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
                entityManager.merge(comment);

            }

            hashMap.put("status","200");
        }catch (Exception e){
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }

    @Override
    public Map<String, Object> getComment(Long trackId, Long memberId) {

        
        Map<String,Object> hashMap = new HashMap<>();
        QComment qComment = QComment.comment;

        try{

            List<Comment> resultComment = jpaQueryFactory.selectFrom(qComment)
                    .where(qComment.track.trackId.eq(trackId))
                    .fetch();

            List<CommentDTO> commentList = new ArrayList<>();

            for (Comment comment : resultComment) {
                if(comment.getParentComment() == null){
                    boolean isLikeComment = false;
                    if (!comment.getCommentLikeList().isEmpty()) {
                        isLikeComment = isCommentLike(comment,memberId);
                    }

                    CommentDTO commentDTO = commentModelMapper(comment,isLikeComment);

                    if (!comment.getChildComments().isEmpty()){
                        commentDTO.setChildCommentActive(true);
                    } else {
                        commentDTO.setChildCommentActive(false);
                    }

                    commentList.add(commentDTO);
                }

            }


            hashMap.put("commentList",commentList);
            hashMap.put("status","200");

        } catch (Exception e ){
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    @Override
    public Map<String, Object> getChildComment(Long commentId, Long memberId) {

        
        QComment qComment = QComment.comment;

        Map<String, Object> hashMap = new HashMap<>();

        try {
            Comment comment = jpaQueryFactory.selectFrom(qComment)
                    .where(qComment.commentId.eq(commentId))
                    .fetchFirst();

            boolean isLikeComment = false;
            if (!comment.getCommentLikeList().isEmpty()) {
                isLikeComment = isCommentLike(comment,memberId);
            }

            CommentDTO commentDTO = commentModelMapper(comment,isLikeComment);

            List<CommentDTO> childCommentList = new ArrayList<>();
            childCommentList = addAllChildComments(childCommentList,comment,memberId);


            hashMap.put("comment",commentDTO);
            hashMap.put("childComment",childCommentList);
            hashMap.put("status","200");
        } catch (Exception e ){
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private CommentDTO commentModelMapper(Comment comment, boolean isLikeComment) {
        return CommentDTO.builder()
                .commentId(comment.getCommentId())
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

    @Override
    public Long getTrackCommentCnt(Long memberId, Long trackId) {
        
        QComment qComment = QComment.comment;
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory
                .select(qComment.count()) // 댓글의 개수를 계산
                .from(qMemberTrack)
                .leftJoin(qMemberTrack.track.commentList, qComment) // Track과 commentList를 조인
                .where(qMemberTrack.track.trackId.eq(trackId))
                .fetchOne(); // 결과를 하나의 값으로 가져옵니다.
    }


    private boolean isCommentLike(Comment comment, Long memberId) {
        return comment.getCommentLikeList().stream()
                .anyMatch(commentLike ->
                        commentLike.getMember().getMemberId().equals(memberId) &&
                                commentLike.isCommentLikeStatus());
    }

    private List<CommentDTO> addAllChildComments(List<CommentDTO> childCommentList,
                                    Comment parentComment,
                                    Long memberId) {
        // 현재 부모 댓글에 대한 자식 댓글들을 가져옴
        for(Comment childComment : parentComment.getChildComments()) {

            // 현재 자식 댓글이 좋아요 여부를 체크
            boolean isLikeChildComment = false;
            if (!childComment.getCommentLikeList().isEmpty()) {
                isLikeChildComment = childComment.getCommentLikeList().stream()
                        .anyMatch(commentLike ->
                                commentLike.getMember().getMemberId().equals(memberId) &&
                                        commentLike.isCommentLikeStatus());
            }

            // 자식 댓글을 DTO로 변환
            CommentDTO childCommentDTO = CommentDTO.builder()
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
            childCommentList.add(childCommentDTO);

            // 자식 댓글이 또 자식 댓글을 가질 수 있으므로, 재귀적으로 호출
            addAllChildComments(childCommentList, childComment, memberId);
        }

        return childCommentList;
    }



}
