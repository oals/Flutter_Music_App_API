package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FollowDto;
import com.skrrskrr.project.dto.FcmSendDto;
import com.skrrskrr.project.dto.FollowRequestDto;
import com.skrrskrr.project.entity.Follow;
import com.skrrskrr.project.entity.Member;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;
import com.skrrskrr.project.queryBuilder.select.FollowSelectQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;

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
    public Map<String, Object> setFollow(FollowRequestDto followRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            Member follower = getFollowMember(followRequestDto.getFollowerId());
            Member following = getFollowMember(followRequestDto.getFollowingId());

            Follow followResult = isFollowStatus(followRequestDto);

            if (followResult != null) {
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
                            .notificationMemberId(followRequestDto.getFollowingId())
                            .memberId(followRequestDto.getFollowerId())
                            .build();

                    fireBaseService.sendPushNotification(fcmSendDTO);
                } catch(Exception e) {
                    hashMap.put("status","500");
                    e.printStackTrace();
                }
            }

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
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


    private Follow isFollowStatus(FollowRequestDto followRequestDto){

        QFollow qFollow = QFollow.follow;

        return jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(followRequestDto.getFollowerId())
                        .and(qFollow.following.memberId.eq(followRequestDto.getFollowingId())))
                .fetchFirst();
    }


    private Member getFollowMember(Long followId){

        QMember qMember = QMember.member;

        return jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(followId))
                .fetchFirst();
    }

    private void updateFollowCounts(Member follower, Member following, Long delta) {

        follower.setMemberFollowerCnt(follower.getMemberFollowerCnt() + delta);
        following.setMemberFollowCnt(following.getMemberFollowCnt() + delta);

        // 변경 사항을 DB에 즉시 반영
        entityManager.flush();
    }


    // 4. 메인 getFollow 메서드
    @Override
    public Map<String, Object> getFollow(FollowRequestDto followRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            List<Follow> followingList = getFollowingList(followRequestDto.getLoginMemberId());
            List<Follow> followerList = getFollowerList(followRequestDto.getLoginMemberId());

            List<FollowDto> followingDtoList = new ArrayList<>();
            List<FollowDto> followerDtoList = new ArrayList<>();

            // followingList에 대한 DTO 생성
            for (Follow following : followingList) {
                FollowDto followingDTO = mapToFollowDTO(following.getFollowing(), 2L);


                Boolean isMutualFollow = isMutualFollow(following, followerList);
                followingDTO.setIsMutualFollow(isMutualFollow);
                if (isMutualFollow) {
                    followingDTO.setIsFollowedCd(3L);
                }
                followingDtoList.add(followingDTO);
            }

            // followerList에 대한 DTO 생성
            for (Follow follower : followerList) {
                FollowDto followerDTO = mapToFollowDTO(follower.getFollower(), 1L);


                Boolean isMutualFollow = isMutualFollow(follower, followingList);
                followerDTO.setIsMutualFollow(isMutualFollow);
                if (isMutualFollow) {
                    followerDTO.setIsFollowedCd(3L);
                }
                followerDtoList.add(followerDTO);
            }

            hashMap.put("followingList", followingDtoList);
            hashMap.put("followerList", followerDtoList);
            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    // 1. FollowList 가져오는 메서드들
    private List<Follow> getFollowingList(Long loginMemberId) {


        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);


        List<Follow> followList = followSelectQueryBuilder.selectFrom(QFollow.follow)
                .findFollowingMember(loginMemberId)
                .fetch(Follow.class);

        return followList;

    }

    // 2. FollowDto로 매핑하는 메서드
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


        List<Follow> followList = followSelectQueryBuilder.selectFrom(QFollow.follow)
                .findFollowerMember(loginMemberId)
                .fetch(Follow.class);

        return followList;
    }


    private Boolean isMutualFollow(Follow follow, List<Follow> otherList) {
        for (Follow otherFollow : otherList) {
            if (otherFollow.getFollower().getMemberId().equals(follow.getFollowing().getMemberId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean isFollowCheck(Long followerId, Long followingId) {
        QFollow qFollow = QFollow.follow;

        Follow follow = jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(followerId)
                        .and(qFollow.following.memberId.eq(followingId)))
                .fetchFirst();

        return follow != null;

    }
}
