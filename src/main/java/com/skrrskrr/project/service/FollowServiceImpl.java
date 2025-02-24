package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FollowDTO;
import com.skrrskrr.project.entity.Follow;
import com.skrrskrr.project.entity.Member;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;
import com.skrrskrr.project.repository.FollowRepository;
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

    private final FollowRepository followRepository;
    private final FireBaseService fireBaseService;
    @PersistenceContext
    EntityManager em;

    @Override
    public Map<String, Object> setFollow(Long followerId, Long followingId) {

        Map<String,Object> hashMap = new HashMap<>();
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QFollow qFollow = QFollow.follow;
        QMember qMember = QMember.member;

        try {

            Member follower = jpaQueryFactory.selectFrom(qMember)
                    .where(qMember.memberId.eq(followerId))
                    .fetchFirst();

            Member following = jpaQueryFactory.selectFrom(qMember)
                    .where(qMember.memberId.eq(followingId))
                    .fetchFirst();

            Follow followResult = jpaQueryFactory.selectFrom(qFollow)
                    .where(qFollow.follower.memberId.eq(followerId)
                            .and(qFollow.following.memberId.eq(followingId)))
                    .fetchFirst();

            if (followResult != null) {
                // 팔로우, 팔로워 삭제
                jpaQueryFactory.delete(qFollow)
                        .where(qFollow.follower.memberId.eq(followerId)
                                .and(qFollow.following.memberId.eq(followingId)))
                        .execute();

                updateFollowCounts(follower, following, -1L);

            } else {
                // 팔로우, 팔로워 등록
                Follow follow = new Follow();
                follow.setFollower(follower);
                follow.setFollowing(following);

                em.persist(follow);

                updateFollowCounts(follower, following, 1L);


                try{
                    fireBaseService.sendPushNotification(
                            followerId,
                            "알림",
                            "회원님을 팔로우 했습니다.",
                            3L,
                            null,
                            null,
                            followingId
                    );
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


    private void updateFollowCounts(Member follower, Member following, Long delta) {


        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        // Member 엔티티의 필드를 직접 수정

        log.info("테테스트123");
        log.info(follower.getMemberFollowerCnt());
        log.info(following.getMemberFollowerCnt());
        log.info(delta);

        follower.setMemberFollowerCnt(follower.getMemberFollowerCnt() + delta);
        following.setMemberFollowCnt(following.getMemberFollowCnt() + delta);

        // 변경 사항을 DB에 즉시 반영
        em.flush();
    }



    @Override
    public Map<String, Object> getFollow(Long memberId) {
        Map<String,Object> hashMap = new HashMap<>();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QFollow qFollow = QFollow.follow;

        try {
            List<Follow> followingList = jpaQueryFactory.selectFrom(qFollow)
                    .where(qFollow.follower.memberId.eq(memberId))
                    .fetch();


            List<Follow> followerList = jpaQueryFactory.selectFrom(qFollow)
                    .where(qFollow.following.memberId.eq(memberId))
                    .fetch();

            List<FollowDTO> followingDtoList = new ArrayList<>();
            List<FollowDTO> followerDtoList = new ArrayList<>();


            for (Follow following : followingList) {
                FollowDTO followingDTO = FollowDTO.builder()
                        .followMemberId(following.getFollowing().getMemberId())
                        .followNickName(following.getFollowing().getMemberNickName())
                        .followImagePath(following.getFollowing().getMemberImagePath())
                        .isFollowedCd(2L)
                        .build();

                // 맞팔 여부 확인 (followerList에서 같은 회원을 팔로우하는지 체크)
                boolean isMutualFollow = false;
                for (Follow follower : followerList) {
                    if (follower.getFollower().getMemberId().equals(following.getFollowing().getMemberId())) {
                        isMutualFollow = true;
                        followingDTO.setIsFollowedCd(3L);
                        break;
                    }
                }

                followingDTO.setMutualFollow(isMutualFollow);
                followingDtoList.add(followingDTO);
            }

            for (Follow follower : followerList) {
                FollowDTO followerDTO = FollowDTO.builder()
                        .followMemberId(follower.getFollower().getMemberId())
                        .followNickName(follower.getFollower().getMemberNickName())
                        .followImagePath(follower.getFollower().getMemberImagePath())
                        .isFollowedCd(1L)
                        .build();

                // 맞팔 여부 확인 (followingList에서 같은 회원을 팔로우하는지 체크)
                boolean isMutualFollow = false;
                for (Follow following : followingList) {
                    if (following.getFollowing().getMemberId().equals(follower.getFollower().getMemberId())) {
                        isMutualFollow = true;
                        followerDTO.setIsFollowedCd(3L);
                        break;
                    }
                }

                // DTO에 맞팔 여부 추가
                followerDTO.setMutualFollow(isMutualFollow);
                followerDtoList.add(followerDTO);
            }

            hashMap.put("followingList",followerDtoList);
            hashMap.put("followerList",followingDtoList);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    @Override
    public Map<String, Object> isFollowCheck(Long followerId, Long followingId) {

        Map<String,Object> hashMap = new HashMap<>();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QFollow qFollow = QFollow.follow;

        Follow follow = jpaQueryFactory.selectFrom(qFollow)
                .where(qFollow.follower.memberId.eq(followerId)
                        .and(qFollow.following.memberId.eq(followingId)))
                .fetchFirst();


        if (follow != null) {
            hashMap.put("followStatus",true);
        } else {
            hashMap.put("followStatus",false);
        }


        return hashMap;
    }
}
