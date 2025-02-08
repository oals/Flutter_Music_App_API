package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.CommentDTO;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CommentServiceImpl implements CommentService{


    @PersistenceContext
    EntityManager em;


    private final FireBaseService fireBaseService;

    private final CommentRepository commentRepository;


    @Override
    public HashMap<String, Object> setComment(Long trackId, Long memberId, String commentText, Long commentId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QTrack qTrack = QTrack.track;


        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        Long fcmMsgTrgtMemberId = null;

        try {
            Member member = Member.builder()
                    .memberId(memberId)
                    .build();

            Track track = jpaQueryFactory.selectFrom(qTrack)
                    .where(qTrack.trackId.eq(trackId))
                    .fetchOne();

            assert track != null;
            fcmMsgTrgtMemberId = track.getMemberTrackList().get(0).getMember().getMemberId();

            log.info("곡주인 아이디");
            log.info(fcmMsgTrgtMemberId);

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
                fcmMsgTrgtMemberId = parentComment.getMember().getMemberId();


                parentComment.getChildComments().add(insertComment);

                insertComment.setParentComment(parentComment);
            }

            em.persist(insertComment);

            try{
                if(!Objects.equals(memberId, fcmMsgTrgtMemberId)){
                    fireBaseService.sendPushNotification(fcmMsgTrgtMemberId,
                            "알림",
                            commentText,
                            2L,
                             trackId,
                            null,
                             null
                            );
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            // 성공 시
            resultMap.put("status", "OK");
        } catch (Exception e) {
            // 실패 시
            e.printStackTrace();
            resultMap.put("status", "FAIL");
        }

        return resultMap; // 최종 결과 반환
    }


    @Override
    public HashMap<String, Object> setCommentLike(Long commentId, Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QCommentLike qCommentLike = QCommentLike.commentLike;
        QComment qComment = QComment.comment;
        HashMap<String,Object> hashMap = new HashMap<>();

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

                em.merge(comment1);

            } else {
                /// insert

                CommentLike insertCommentLike = new CommentLike();
                insertCommentLike.setComment(comment);
                insertCommentLike.setCommentLikeStatus(true);
                insertCommentLike.setMember(member);

                em.persist(insertCommentLike);


                comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
                em.merge(comment);

            }

            hashMap.put("status","200");
        }catch (Exception e){
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }

    @Override
    public HashMap<String, Object> getComment(Long trackId, Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        HashMap<String,Object> hashMap = new HashMap<>();
        QComment qComment = QComment.comment;
        QCommentLike qCommentLike = QCommentLike.commentLike;


        try{

            List<Comment> resultComment = jpaQueryFactory.selectFrom(qComment)
                    .where(qComment.track.trackId.eq(trackId))
                    .fetch();

            List<CommentDTO> commentList = new ArrayList<>();

            for (Comment comment : resultComment) {
                if(comment.getParentComment() == null){
                    boolean isLikeComment = false;
                    if (!comment.getCommentLikeList().isEmpty()) {
                        isLikeComment = comment.getCommentLikeList().stream()
                                .anyMatch(commentLike ->
                                        commentLike.getMember().getMemberId().equals(memberId) &&
                                                commentLike.isCommentLikeStatus());

                    }

                    CommentDTO commentDTO = CommentDTO.builder()
                            .commentId(comment.getCommentId())
                            .commentText(comment.getCommentText())
                            .commentDate(comment.getCommentDate())
                            .trackId(comment.getTrack().getTrackId())
                            .memberId(comment.getMember().getMemberId())
                            .commentLikeCnt(comment.getCommentLikeCnt())
                            .commentLikeStatus(isLikeComment)
                            .memberNickName(comment.getMember().getMemberNickName())
                            .memberImagePath(comment.getMember().getMemberImagePath())
                            .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getCommentId())
                            .build();

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
    public HashMap<String, Object> getChildComment(Long commentId, Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QComment qComment = QComment.comment;

        HashMap<String, Object> hashMap = new HashMap<>();

        try {
            Comment comment = jpaQueryFactory.selectFrom(qComment)
                    .where(qComment.commentId.eq(commentId))
                    .fetchFirst();

            boolean isLikeComment = false;
            if (!comment.getCommentLikeList().isEmpty()) {
                isLikeComment = comment.getCommentLikeList().stream()
                        .anyMatch(commentLike ->
                                commentLike.getMember().getMemberId().equals(memberId) &&
                                        commentLike.isCommentLikeStatus());
            }

            CommentDTO commentDTO = CommentDTO.builder()
                    .commentId(comment.getCommentId())
                    .commentText(comment.getCommentText())
                    .commentDate(comment.getCommentDate())
                    .commentLikeCnt(comment.getCommentLikeCnt())
                    .commentLikeStatus(isLikeComment)
                    .trackId(comment.getTrack().getTrackId())
                    .memberId(comment.getMember().getMemberId())
                    .memberNickName(comment.getMember().getMemberNickName())
                    .memberImagePath(comment.getMember().getMemberImagePath())
                    .build();


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



    public List<CommentDTO>  addAllChildComments(List<CommentDTO> childCommentList,
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
