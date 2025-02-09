package com.skrrskrr.project.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.MemberDTO;
import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MainServiceImpl implements MainService {

    @PersistenceContext
    EntityManager em;

    private final MemberRepository memberRepository;

    @Override
    public Map<String, Object> firstLoad(Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        Map<String, Object> hashMap = new HashMap<>();

        QMember qMember = QMember.member;
        QTrack qTrack = QTrack.track;
        QPlayList qPlayList = QPlayList.playList;
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QFollow qFollow = QFollow.follow;
        QTrackLike qTrackLike = QTrackLike.trackLike;
        QNotifications qNotifications = QNotifications.notifications;

        try {
            //내 멤버 엔티티
            Member myMember = jpaQueryFactory.selectFrom(qMember)
                    .where(qMember.memberId.eq(memberId))
                    .fetchFirst();


            boolean notificationIsView = Boolean.FALSE.equals(
                    jpaQueryFactory.select(
                                    qNotifications.notificationIsView
                            ).from(qNotifications)
                            .where(qNotifications.member.memberId.eq(memberId)
                                    .and(qNotifications.notificationIsView.isFalse()))
                            .fetchFirst()
            );
            hashMap.put("notificationIsView",notificationIsView);



            /// 인기 앨범 추천  - 카테고리에 해당하는 곡의 수 , 조회수, 좋아요 수 ,
            List<PlayList> queryResultPlayList = jpaQueryFactory.selectFrom(qPlayList)
                    .join(qPlayList.playListTrackList, qTrack)
                    .groupBy(qPlayList.playListId)  // PlayList 단위로 그룹화
                    .orderBy(
                            qTrack.trackPlayCnt.count().desc(),      // trackPlayCnt의 등장 횟수
                            qTrack.trackLikeCnt.count().desc()
                    )
                    .where(qPlayList.playListTrackList.isNotEmpty()
                            .and(qPlayList.isPlayListPrivacy.isFalse()))  // Track 리스트가 비어 있지 않은 PlayList만
                    .limit(4)  // 상위 8개만 조회
                    .fetch();


            List<PlayListDTO> popularPlayList = new ArrayList<>();
            for (PlayList playList : queryResultPlayList) {
                PlayListDTO playListDTO = PlayListDTO.builder()
                        .playListId(playList.getPlayListId())
                        .playListNm(playList.getPlayListNm())
                        .memberNickName(playList.getMember().getMemberNickName())
                        .playListImagePath(playList.getPlayListTrackList().get(0).getTrackImagePath())
                        .build();

                popularPlayList.add(playListDTO);
            }

            hashMap.put("popularPlayList",popularPlayList);



            /// 내가 팔로우 한 유저의 곡 , 최신날짜 ,
            List<MemberTrack> queryResultFollowMemberTrack = jpaQueryFactory
                    .selectFrom(qMemberTrack)
                    .join(qMemberTrack.member, qMember) // MemberTrack과 Member를 조인
                    .join(qMember.followers, qFollow)   // Member와 Follow를 조인 (팔로우 관계)
                    .where(qFollow.following.eq(myMember)
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse()))  // 내가 팔로우한 유저들
                    .orderBy(qMemberTrack.track.trackUploadDate.desc()) // 최신 날짜 순으로 정렬
                    .limit(4)
                    .fetch();



            List<TrackDTO> followMemberTrackList = new ArrayList<>();
            for (MemberTrack track : queryResultFollowMemberTrack) {
                TrackDTO trackDTO = TrackDTO.builder()
                        .trackId(track.getTrack().getTrackId())
                        .trackNm(track.getTrack().getTrackNm())
                        .trackImagePath(track.getTrack().getTrackImagePath())
                        .memberNickName(track.getMember().getMemberNickName())
                        .memberId(track.getMember().getMemberId())
                        .build();


                followMemberTrackList.add(trackDTO);
            }

            hashMap.put("followMemberTrackList",followMemberTrackList);



            /// 관심 트랙 - 관심트랙 리스트에서 랜덤 , 선택된 카테고리 ,
            List<TrackLike> queryResultLikedTrack = jpaQueryFactory.selectFrom(qTrackLike)
                    .where(qTrackLike.trackLikeStatus.isTrue()
                            .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                            .and(qTrackLike.member.eq(myMember)))
                    .groupBy(qTrackLike.memberTrack.track.trackId) // 멤버 아이디별로 그룹화
                    .orderBy(
                            Expressions.numberTemplate(Double.class, "function('RAND')").asc()  // 랜덤 정렬
                    ).limit(4)
                    .fetch();

            List<TrackDTO> likedTrackList = new ArrayList<>();
            for (TrackLike trackLike : queryResultLikedTrack) {
                TrackDTO trackDTO = TrackDTO.builder()
                        .trackId(trackLike.getMemberTrack().getTrack().getTrackId())
                        .trackNm(trackLike.getMemberTrack().getTrack().getTrackNm())
                        .trackImagePath(trackLike.getMemberTrack().getTrack().getTrackImagePath())
                        .memberNickName(trackLike.getMemberTrack().getMember().getMemberNickName())
                        .memberId(trackLike.getMemberTrack().getMember().getMemberId())
                        .build();

                likedTrackList.add(trackDTO);
            }


            hashMap.put("likedTrackList",likedTrackList);




            /// 인기 유저 추천 -  곡 한개 이상 업로드 ~ / 선택된 카테고리 (카테고리는 폰에 캐시로 저장 )
            /// 팔로우 여부 필요
            List<MemberTrack> queryResultMember = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(
                            qMemberTrack.member.memberId.ne(memberId) // 자기 자신 제외
                                    .and(qMemberTrack.member.memberTrackList.isNotEmpty())  // 트랙 리스트가 비어 있지 않음
                    )
                    .groupBy(qMemberTrack.member.memberId) // 멤버 아이디별로 그룹화
                    .orderBy(
                            Expressions.numberTemplate(Double.class, "function('RAND')").asc()  // 랜덤 정렬
                    )
                    .limit(8)  // 랜덤으로 8개만 추출
                    .fetch();

            List<MemberDTO> randomMemberList = new ArrayList<>();

            for (MemberTrack randomItem : queryResultMember) {

                int isFollowedCd = 0; // 관계없음

                for (int i = 0; i < randomItem.getMember().getFollowers().size(); i++) {
                    if (randomItem.getMember().getFollowers().get(i).getFollowing().getMemberId().equals(myMember.getMemberId())) {
                        isFollowedCd = 1; // 내가 팔로우 중
                        break;
                    }
                }

                for (int i = 0; i < randomItem.getMember().getFollowing().size(); i++) {
                    if (randomItem.getMember().getFollowing().get(i).getFollower().getMemberId().equals(myMember.getMemberId())) {
                        if(isFollowedCd == 1){
                            isFollowedCd = 3; //맞팔
                        } else {
                            isFollowedCd = 2; //내 팔로워
                        }
                    }
                }


                MemberDTO memberDTO = MemberDTO.builder()
                        .memberId(randomItem.getMember().getMemberId())
                        .memberNickName(randomItem.getMember().getMemberNickName())
                        .memberImagePath(randomItem.getMember().getMemberImagePath())
                        .isFollowedCd(isFollowedCd)
                        .build();

                randomMemberList.add(memberDTO);
            }

            hashMap.put("randomMemberList", randomMemberList);




            /// 트렌딩 음원 추천
            List<MemberTrack> queryResultTrackPlayCnt = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                    ).orderBy(qMemberTrack.track.trackPlayCnt.desc())
                    .limit(5)  // 예: 결과 10개로 제한
                    .fetch();

            List<MemberTrack> queryResultTrackLikeCnt = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                    ).orderBy(qMemberTrack.track.trackPlayCnt.desc())
                    .limit(5)  // 예: 결과 10개로 제한
                    .fetch();

            List<MemberTrack> combinedResult = new ArrayList<>();
            combinedResult.addAll(queryResultTrackPlayCnt);
            combinedResult.addAll(queryResultTrackLikeCnt);

            // 중복 제거 (필요한 경우)
            combinedResult = combinedResult.stream()
                    .distinct()
                    .collect(Collectors.toList());

            List<TrackDTO> trendingTrackList = new ArrayList<>();
            for (MemberTrack memberTrack : combinedResult) {
                TrackDTO trackDTO = TrackDTO.builder()
                        .trackId(memberTrack.getTrack().getTrackId())
                        .trackNm(memberTrack.getTrack().getTrackNm())
                        .trackPlayCnt(memberTrack.getTrack().getTrackPlayCnt())
                        .trackLikeCnt(memberTrack.getTrack().getTrackLikeCnt())
                        .trackImagePath(memberTrack.getTrack().getTrackImagePath())
                        .memberNickName(memberTrack.getMember().getMemberNickName())
                        .build();

                trendingTrackList.add(trackDTO);

            }
            hashMap.put("trendingTrackList", trendingTrackList);
            hashMap.put("status","200");
        } catch (Exception e) {
            hashMap.put("status","500");
        }


        return hashMap;
    }


    public BooleanExpression isUploadedThisWeekOrLastWeek(StringExpression trackUploadDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 날짜 기준으로 이번 주와 저번 주의 날짜 범위를 계산합니다.
        LocalDate today = LocalDate.now();

        // 이번 주 시작일과 종료일
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfThisWeek = startOfThisWeek.plusDays(6);  // 일요일까지

        // 저번 주 시작일과 종료일
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

        // 날짜를 문자열로 변환
        String startOfThisWeekStr = startOfThisWeek.format(formatter);
        String endOfThisWeekStr = endOfThisWeek.format(formatter);

        String startOfLastWeekStr = startOfLastWeek.format(formatter);
        String endOfLastWeekStr = endOfLastWeek.format(formatter);

        // 이번 주 또는 저번 주 업로드 날짜 필터
        BooleanExpression uploadThisWeek = trackUploadDate.between(startOfThisWeekStr, endOfThisWeekStr);
        BooleanExpression uploadLastWeek = trackUploadDate.between(startOfLastWeekStr, endOfLastWeekStr);

        return uploadThisWeek.or(uploadLastWeek);
    }

}
