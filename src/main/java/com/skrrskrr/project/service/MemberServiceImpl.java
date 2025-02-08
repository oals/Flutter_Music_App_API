package com.skrrskrr.project.service;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.MemberDTO;
import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class MemberServiceImpl implements MemberService {

    @PersistenceContext
    EntityManager em;

    private final MemberRepository memberRepository;


    @Override
    public MemberDTO getMemberInfo(String memberEmail) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qmember = QMember.member; // QMember 타입 사용

        // QueryDSL을 사용하여 authToken으로 Member 조회
        Member foundMember = queryFactory
                .selectFrom(qmember)
                .where(qmember.memberEmail.eq(memberEmail))
                .fetchOne(); // 단일 결과를 가져옴

        if (foundMember != null) {
            return EntityToDto(foundMember);
        } else {
            return null;
        }

    }

    @Override
    public HashMap<String,Object> setMemberDeviceToken(MemberDTO memberDTO) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;
        HashMap<String,Object> hashMap = new HashMap<>();
        try {
            queryFactory.update(qMember)
                    .set(qMember.memberDeviceToken, memberDTO.getDeviceToken())
                    .where(qMember.memberId.eq(memberDTO.getMemberId()))
                    .execute();
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public HashMap<String,Object> setMemberImage(UploadDTO uploadDTO) {
        HashMap<String,Object> hashMap = new HashMap<>();
        try {

            JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            QMember qMember = QMember.member;

            queryFactory.update(qMember)
                    .set(qMember.memberImagePath, uploadDTO.getUploadImagePath())
                    .where(qMember.memberId.eq(uploadDTO.getMemberId()))
                    .execute();
            hashMap.put("status","200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
            return hashMap;
        }
    }


    @Override
    public HashMap<String,Object> setMemberInfoUpdate(MemberDTO memberDTO) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;
        HashMap<String,Object> hashMap = new HashMap<>();

        try {
            // 동적으로 memberNickName과 memberInfo를 설정하는 방식
            jpaQueryFactory.update(qMember)
                    .set(qMember.memberNickName,
                            memberDTO.getMemberNickName() != null ?
                                    Expressions.constant(memberDTO.getMemberNickName()) : qMember.memberNickName)
                    .set(qMember.memberInfo,
                            memberDTO.getMemberNickName() == null ?
                                    Expressions.constant(memberDTO.getMemberInfo()) : qMember.memberInfo)
                    .where(qMember.memberId.eq(memberDTO.getMemberId()))
                    .execute();
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public HashMap<String,Object> setMemberInfo(String memberEmail, String deviceToken) {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        HashMap<String,Object> hashMap = new HashMap<>();

        try {
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }

            MemberDTO memberDTO = MemberDTO.builder()
                    .memberNickName(memberEmail.split("@")[0] + "_" + sb)
                    .memberEmail(memberEmail)
                    .memberDeviceToken(deviceToken)
                    .build();

            Member member = Member.createMember(memberDTO);
            Member member1 = memberRepository.save(member);

            memberDTO.setMemberId(member1.getMemberId());
            hashMap.put("member", memberDTO);
            hashMap.put("status","200");
        } catch (Exception e ){
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public HashMap<String, Object> getMemberPageInfo(Long memberId, Long loginMemberId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        List<PlayListDTO> playListDtoList = new ArrayList<>();
        List<TrackDTO> popularTrackDtoList = new ArrayList<>();
        List<TrackDTO> allTrackDtoList = new ArrayList<>();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;

        try {

            MemberTrack queryResultMemberTrack = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(qMemberTrack.member.memberId.eq(memberId))
                    .fetchFirst();

            // 회원 정보 조회
            MemberDTO memberDTO;
            if (queryResultMemberTrack == null) {
                Member queryResultMember = jpaQueryFactory.selectFrom(qMember)
                        .where(qMember.memberId.eq(memberId))
                        .fetchFirst();

                memberDTO = MemberDTO.builder()
                        .memberId(queryResultMember.getMemberId())
                        .memberNickName(queryResultMember.getMemberNickName())
                        .memberInfo(queryResultMember.getMemberInfo())
                        .memberEmail(queryResultMember.getMemberEmail())
                        .memberFollowCnt(queryResultMember.getMemberFollowCnt())
                        .memberFollowerCnt(queryResultMember.getMemberFollowerCnt())
                        .memberImagePath(queryResultMember.getMemberImagePath())
                        .build();
            } else {
                memberDTO = MemberDTO.builder()
                        .memberId(queryResultMemberTrack.getMember().getMemberId())
                        .memberNickName(queryResultMemberTrack.getMember().getMemberNickName())
                        .memberInfo(queryResultMemberTrack.getMember().getMemberInfo())
                        .memberEmail(queryResultMemberTrack.getMember().getMemberEmail())
                        .memberFollowCnt(queryResultMemberTrack.getMember().getMemberFollowCnt())
                        .memberFollowerCnt(queryResultMemberTrack.getMember().getMemberFollowerCnt())
                        .memberImagePath(queryResultMemberTrack.getMember().getMemberImagePath())
                        .build();

            }

            // 플레이리스트 조회
            List<MemberPlayList> memberPlayList = jpaQueryFactory
                    .selectFrom(qMemberPlayList)
                    .where(qMemberPlayList.member.memberId.eq(memberId)
                            .and(qMemberPlayList.playList.isPlayListPrivacy.isFalse()
                                    .or(qMemberPlayList.member.memberId.eq(loginMemberId))))
                    .limit(5)
                    .fetch();

            for (MemberPlayList playList : memberPlayList) {
                String playListFirstTrackImagePath = !playList.getPlayList().getPlayListTrackList().isEmpty()
                        ? playList.getPlayList().getPlayListTrackList().get(0).getTrackImagePath()
                        : "";

                PlayListDTO playListDTO = PlayListDTO.builder()
                        .playListId(playList.getPlayList().getPlayListId())
                        .playListNm(playList.getPlayList().getPlayListNm())
                        .playListImagePath(playListFirstTrackImagePath)
                        .memberNickName(playList.getMember().getMemberNickName())
                        .memberId(playList.getPlayList().getMember().getMemberId())
                        .build();

                playListDtoList.add(playListDTO);
            }

            // 인기 트랙 및 모든 트랙 조회
            for (boolean isPopular : new boolean[]{true, false}) {
                List<MemberTrack> trackList = jpaQueryFactory.selectFrom(qMemberTrack)
                        .where(qMemberTrack.member.memberId.eq(memberId)
                                .and(qMemberTrack.track.isTrackPrivacy.isFalse()
                                        .or(qMemberTrack.member.memberId.eq(loginMemberId))))
                        .orderBy(isPopular ? qMemberTrack.track.trackPlayCnt.desc() : qMemberTrack.track.trackId.desc())
                        .limit(isPopular ? 4 : 7)
                        .fetch();

                List<TrackDTO> trackDtoList = new ArrayList<>();
                for (MemberTrack memberTrack : trackList) {
                    TrackDTO trackDTO = TrackDTO.builder()
                            .trackId(memberTrack.getTrack().getTrackId())
                            .trackNm(memberTrack.getTrack().getTrackNm())
                            .trackImagePath(memberTrack.getTrack().getTrackImagePath())
                            .trackCategoryId(memberTrack.getTrack().getTrackCategoryId())
                            .trackTime(memberTrack.getTrack().getTrackTime())
                            .build();
                    trackDtoList.add(trackDTO);
                }

                if (isPopular) {
                    popularTrackDtoList = trackDtoList;
                } else {
                    allTrackDtoList = trackDtoList;
                }
            }

            hashMap.put("memberDTO", memberDTO);
            hashMap.put("playListDTO", playListDtoList);
            hashMap.put("popularTrackList", popularTrackDtoList);
            hashMap.put("allTrackList", allTrackDtoList);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

}
