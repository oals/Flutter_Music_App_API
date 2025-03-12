package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.config.ModelMapperConfig;
import com.skrrskrr.project.dto.FollowDTO;
import com.skrrskrr.project.dto.FcmSendDTO;
import com.skrrskrr.project.entity.Follow;
import com.skrrskrr.project.entity.Member;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Override
    public Map<String, Object> setFollow(Long followerId, Long followingId) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            Member follower = getFollowMember(followerId);
            Member following = getFollowMember(followingId);

            Follow followResult = isFollowStatus(followerId,followingId);

            if (followResult != null) {
                // 팔로우, 팔로워 삭제
                deleteFollow(followerId,followingId);
                updateFollowCounts(follower, following, -1L);
            } else {
                // 팔로우, 팔로워 등록
                insertFollow(follower,following);
                updateFollowCounts(follower, following, 1L);

                try{
                    FcmSendDTO fcmSendDTO = FcmSendDTO.builder()
                            .title("알림")
                            .body("회원님을 팔로우 했습니다.")
                            .notificationType(3L)
                            .notificationMemberId(followingId)
                            .memberId(followerId)
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

    private void deleteFollow(Long followerId, Long followingId){

        QFollow qFollow = QFollow.follow;

        jpaQueryFactory.delete(qFollow)
                .where(qFollow.follower.memberId.eq(followerId)
                        .and(qFollow.following.memberId.eq(followingId)))
                .execute();

    }


    private Follow isFollowStatus(Long followerId, Long followingId){

        QFollow qFollow = QFollow.follow;

        return jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(followerId)
                        .and(qFollow.following.memberId.eq(followingId)))
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
    public Map<String, Object> getFollow(Long memberId) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            List<Follow> followingList = getFollowingList(memberId);
            List<Follow> followerList = getFollowerList(memberId);

            List<FollowDTO> followingDtoList = new ArrayList<>();
            List<FollowDTO> followerDtoList = new ArrayList<>();

            // followingList에 대한 DTO 생성
            for (Follow following : followingList) {
                FollowDTO followingDTO = modelMapper.map(following.getFollowing(), FollowDTO.class);
                followingDTO.setIsFollowedCd(2L);


                boolean isMutualFollow = isMutualFollow(following, followerList);
                followingDTO.setMutualFollow(isMutualFollow);
                if (isMutualFollow) {
                    followingDTO.setIsFollowedCd(3L);
                }

                followingDtoList.add(followingDTO);
            }

            // followerList에 대한 DTO 생성
            for (Follow follower : followerList) {
                FollowDTO followerDTO = modelMapper.map(follower.getFollower(), FollowDTO.class);
                followerDTO.setIsFollowedCd(1L);

                boolean isMutualFollow = isMutualFollow(follower, followingList);
                followerDTO.setMutualFollow(isMutualFollow);
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
    private List<Follow> getFollowingList(Long memberId) {
        
        QFollow qFollow = QFollow.follow;
        return jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(memberId))
                .fetch();
    }

    private List<Follow> getFollowerList(Long memberId) {
        
        QFollow qFollow = QFollow.follow;
        return jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.following.memberId.eq(memberId))
                .fetch();
    }


    // 3. 맞팔 여부를 체크하는 메서드
    private boolean isMutualFollow(Follow follow, List<Follow> otherList) {
        for (Follow otherFollow : otherList) {
            if (otherFollow.getFollower().getMemberId().equals(follow.getFollowing().getMemberId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFollowCheck(Long followerId, Long followingId) {

        QFollow qFollow = QFollow.follow;

        Follow follow = jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(followerId)
                        .and(qFollow.following.memberId.eq(followingId)))
                .fetchFirst();


        return follow != null;

    }
}
