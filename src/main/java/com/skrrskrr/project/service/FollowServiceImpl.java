package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.Follow;
import com.skrrskrr.project.entity.Member;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;
import com.skrrskrr.project.queryBuilder.select.FollowSelectQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;




import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class FollowServiceImpl implements FollowService{

    @PersistenceContext
    EntityManager entityManager;
    
    private final JPAQueryFactory jpaQueryFactory;
    private final FireBaseService fireBaseService;

    @Override
    public void setFollow(FollowRequestDto followRequestDto) {

        Member follower = getFollowMember(followRequestDto.getFollowerId());
        Member following = getFollowMember(followRequestDto.getFollowingId());

        Boolean isFollow = isFollowCheck(follower.getMemberId(), following.getMemberId());

        if (isFollow) {
            // 팔로우, 팔로워 삭제
            deleteFollow(followRequestDto);
            updateFollowCounts(follower, following, -1L);
        } else {
            // 팔로우, 팔로워 등록
            insertFollow(follower,following);
            updateFollowCounts(follower, following, 1L);

            try{

                FcmSendDto fcmSendDTO = FcmSendDto.builder()
                        .title("알림")
                        .body(follower.getMemberNickName() + "님이 회원님을 팔로우 했습니다.")
                        .notificationType(3L)
                        .notificationIsView(false)
                        .notificationMemberId(followRequestDto.getFollowerId())
                        .memberId(followRequestDto.getFollowingId())
                        .build();

                fireBaseService.sendPushNotification(fcmSendDTO);


            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insertFollow(Member follower , Member following) {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        entityManager.persist(follow);
    }

    private void deleteFollow(FollowRequestDto followRequestDto ){

        QFollow qFollow = QFollow.follow;

        jpaQueryFactory.delete(qFollow)
                .where(qFollow.follower.memberId.eq(followRequestDto.getFollowerId())
                        .and(qFollow.following.memberId.eq(followRequestDto.getFollowingId())))
                .execute();
    }

    private Member getFollowMember(Long followId){

        QMember qMember = QMember.member;

        return jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(followId))
                .fetchFirst();
    }

    private void updateFollowCounts(Member follower, Member following, Long delta) {

        follower.setMemberFollowCnt(follower.getMemberFollowCnt() + delta);
        following.setMemberFollowerCnt(following.getMemberFollowerCnt() + delta);

        // 변경 사항을 DB에 즉시 반영
        entityManager.flush();
    }

    @Override
    public FollowResponseDto getFollow(FollowRequestDto followRequestDto) {

        List<Follow> followingList = getFollowingList(followRequestDto.getLoginMemberId());
        List<Follow> followerList = getFollowerList(followRequestDto.getLoginMemberId());

        List<FollowDto> followingDtoList = new ArrayList<>();
        List<FollowDto> followerDtoList = new ArrayList<>();

        // followingList에 대한 DTO 생성
        for (Follow following : followingList) {
            FollowDto followingDTO = mapToFollowDTO(following.getFollowing(), 1L);

            Boolean isMutualFollow = isMutualFollow(following, followerList , true);
            followingDTO.setIsMutualFollow(isMutualFollow);
            if (isMutualFollow) {
                followingDTO.setIsFollowedCd(3L);
            }
            followingDtoList.add(followingDTO);
        }

        // followerList에 대한 DTO 생성
        for (Follow follower : followerList) {
            FollowDto followerDTO = mapToFollowDTO(follower.getFollower(), 2L);

            Boolean isMutualFollow = isMutualFollow(follower, followingList, false);
            followerDTO.setIsMutualFollow(isMutualFollow);
            if (isMutualFollow) {
                followerDTO.setIsFollowedCd(3L);
            }
            followerDtoList.add(followerDTO);
        }

        return FollowResponseDto.builder()
                .followingList(followingDtoList)
                .followerList(followerDtoList)
                .build();
    }

    private List<Follow> getFollowingList(Long loginMemberId) {

        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);

        return followSelectQueryBuilder.selectFrom(QFollow.follow)
                .findFollowingMember(loginMemberId)
                .fetch(Follow.class);
    }

    private FollowDto mapToFollowDTO(Member follow, Long isFollowedCd) {
        return FollowDto.builder()
                .followMemberId(follow.getMemberId())
                .followNickName(follow.getMemberNickName())
                .followImagePath(follow.getMemberImagePath())
                .isFollowedCd(isFollowedCd)
                .build();
    }

    private List<Follow> getFollowerList(Long loginMemberId) {

        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);

        return followSelectQueryBuilder.selectFrom(QFollow.follow)
                .findFollowerMember(loginMemberId)
                .fetch(Follow.class);
    }

    private Boolean isMutualFollow(Follow follow, List<Follow> otherList, boolean isFollowingList) {

        if (isFollowingList) {

            for (Follow otherFollow : otherList) {
                if (otherFollow.getFollower().getMemberId().equals(follow.getFollowing().getMemberId())) {
                    return true;
                }
            }

        } else {

            for (Follow otherFollow : otherList) {
                if (otherFollow.getFollowing().getMemberId().equals(follow.getFollower().getMemberId())) {
                    return true;
                }
            }

        }

        return false;
    }

    @Override
    public Boolean isFollowCheck(Long followerId, Long followingId) {

        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);

        return followSelectQueryBuilder.selectFrom(QFollow.follow)
                .findFollowingMember(followerId)
                .findFollowerMember(followingId)
                .fetchOne(Follow.class) != null;

    }
}
