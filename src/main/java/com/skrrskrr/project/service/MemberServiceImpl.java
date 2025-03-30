package com.skrrskrr.project.service;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.MemberSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.MemberUpdateQueryBuilder;
import com.skrrskrr.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class MemberServiceImpl implements MemberService {

    @PersistenceContext
    EntityManager entityManager;

    private final MemberRepository memberRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final PlayListService playListService;
    private final TrackService trackService;
    private final ModelMapper modelMapper;

    @Override
    public Map<String,Object> getMemberInfo(MemberRequestDto memberRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

            MemberDto memberDto = memberSelectQueryBuilder.selectFrom(QMember.member)
                    .findMemberByMemberEmail(memberRequestDto.getMemberEmail())
                    .fetchPreviewMemberDto(MemberDto.class);

            hashMap.put("status","200");
            hashMap.put("member", memberDto);
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;

    }


    @Override
    public Map<String,Object> setMemberDeviceToken(MemberRequestDto memberRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

            memberUpdateQueryBuilder
                    .setEntity(QMember.member)
                    .set(QMember.member.memberDeviceToken, memberRequestDto.getDeviceToken())
                    .findMemberByMemberId(memberRequestDto.getLoginMemberId())
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String,Object> setMemberImage(UploadDto uploadDto) {
        Map<String,Object> hashMap = new HashMap<>();
        try {

            MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

            memberUpdateQueryBuilder
                    .setEntity(QMember.member)
                    .set(QMember.member.memberImagePath, uploadDto.getUploadImagePath())
                    .findMemberByMemberId(uploadDto.getLoginMemberId())
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
    public Map<String,Object> setMemberInfoUpdate(MemberRequestDto memberRequestDto) {

        
        QMember qMember = QMember.member;
        Map<String,Object> hashMap = new HashMap<>();

        try {

            MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

            memberUpdateQueryBuilder
                    .setEntity(QMember.member)
                    .set(QMember.member.memberNickName, memberRequestDto.getMemberNickName())
                    .set(qMember.memberInfo,memberRequestDto.getMemberInfo())
                    .findMemberByMemberId(memberRequestDto.getLoginMemberId())
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String,Object> setMemberInfo(MemberRequestDto memberRequestDto) {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        Map<String,Object> hashMap = new HashMap<>();

        try {
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }

            MemberDto memberDto = MemberDto.builder()
                    .memberNickName(memberRequestDto.getMemberEmail().split("@")[0] + "_" + sb)
                    .memberEmail(memberRequestDto.getMemberEmail())
                    .memberDeviceToken(memberRequestDto.getDeviceToken())
                    .build();

            Member member = Member.createMember(memberDto);
            Member member1 = memberRepository.save(member);

            memberDto.setMemberId(member1.getMemberId());
            hashMap.put("member", memberDto);
            hashMap.put("status","200");
        } catch (Exception e ){
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Member getMemberEntity(Long memberId) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return (Member) memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberId(memberId)
                .fetchOne(Member.class);

    }


    @Override
    public Map<String, Object> getMemberPageInfo(MemberRequestDto memberRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {

            Member member = getMemberEntity(memberRequestDto.getMemberId());
            MemberDto memberDto = EntityToDto(member);

            // 플레이리스트 조회
            Long playListDtoListCnt = playListService.getMemberPlayListCnt(memberRequestDto);
            List<PlayListDto> playListDtoList = new ArrayList<>();
            if(playListDtoListCnt != 0L) {
                memberRequestDto.setLimit(6L);
                playListDtoList = playListService.getMemberPlayList(memberRequestDto);
            }

            List<TrackDto> allTrackDtoList = new ArrayList<>();
            List<TrackDto> popularTrackDtoList = new ArrayList<>();
            Long allTrackDtoListCnt = trackService.getMemberTrackListCnt(memberRequestDto);

            if (allTrackDtoListCnt != 0){
                memberRequestDto.setLimit(10L);
                allTrackDtoList = trackService.getAllMemberTrackList(memberRequestDto);

                memberRequestDto.setLimit(5L);
                popularTrackDtoList = trackService.getPopularMemberTrackList(memberRequestDto);
            }

            hashMap.put("memberDTO", memberDto);
            hashMap.put("playListDTO", playListDtoList);
            hashMap.put("popularTrackList",popularTrackDtoList);
            hashMap.put("allTrackList",allTrackDtoList);
            hashMap.put("playListListCnt", playListDtoListCnt);
            hashMap.put("allTrackListCnt",allTrackDtoListCnt);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public List<FollowDto> getSearchMemberList(SearchRequestDto searchRequestDto) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        List<Member> queryMemberResult = memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberBySearchText(searchRequestDto.getSearchText())
                .findMemberByMemberIdNotEqual(searchRequestDto.getLoginMemberId())
                .offset(searchRequestDto.getOffset())
                .limit(searchRequestDto.getLimit())
                .fetch(Member.class);

        List<FollowDto> searchMemberDtos = new ArrayList<>();

        for (Member member : queryMemberResult) {

            FollowDto followDto = FollowDto.builder()
                    .isFollowedCd(0L)
                    .followImagePath(member.getMemberImagePath())
                    .followMemberId(member.getMemberId())
                    .followNickName(member.getMemberNickName())
                    .isMutualFollow(false)
                    .build();

            if (!member.getFollowers().isEmpty()
                    || !member.getFollowing().isEmpty()) {

                if (!member.getFollowers().isEmpty()) {
                    for (Follow item : member.getFollowers()) {
                        if (item.getFollowing().getMemberId().equals(searchRequestDto.getLoginMemberId())) {
                            followDto.setIsFollowedCd(1L);   // 내가 팔로우
                        }
                    }
                }

                if (!member.getFollowing().isEmpty()) {
                    for (Follow item : member.getFollowing()) {
                        if (item.getFollower().getMemberId().equals(searchRequestDto.getLoginMemberId())) {
                            if (followDto.getIsFollowedCd() == 1L) {
                                followDto.setIsFollowedCd(3L); // 맞팔
                                followDto.setIsMutualFollow(true);
                            } else {
                                followDto.setIsFollowedCd(2L); // 내팔로워
                            }

                        }
                    }
                }
            }
            searchMemberDtos.add(followDto);
        }

        return searchMemberDtos;

    }

    @Override
    public Long getSearchMemberListCnt(SearchRequestDto searchRequestDto) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberBySearchText(searchRequestDto.getSearchText())
                .findMemberByMemberIdNotEqual(searchRequestDto.getLoginMemberId())
                .fetchCount();
    }


    @Override
    public List<MemberDto> getRandomMemberList(MemberRequestDto memberRequestDto){

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        List<Member> memberList = memberSelectQueryBuilder
                .selectFrom(QMember.member)
                .findIsNotEmptyMemberTrackList()
                .findMemberByMemberIdNotEqual(memberRequestDto.getLoginMemberId())
                .groupByMemberId()
                .orderByRandom()
                .limit(memberRequestDto.getLimit())
                .fetch(Member.class);

        return createRandomMmeberDtoList(memberList, memberRequestDto.getLoginMemberId());
    }

    private List<MemberDto> createRandomMmeberDtoList(List<Member> queryResultMember, Long loginMemberId){

        List<MemberDto> randomMemberList = new ArrayList<>();

        for (Member randomItem : queryResultMember) {

            int isFollowedCd = 0; // 관계없음

            for (int i = 0; i < randomItem.getFollowers().size(); i++) {
                if (randomItem.getFollowers().get(i).getFollowing().getMemberId().equals(loginMemberId)) {
                    isFollowedCd = 1; // 내가 팔로우 중
                    break;
                }
            }

            for (int i = 0; i < randomItem.getFollowing().size(); i++) {
                if (randomItem.getFollowing().get(i).getFollower().getMemberId().equals(loginMemberId)) {
                    if(isFollowedCd == 1){
                        isFollowedCd = 3; //맞팔
                    } else {
                        isFollowedCd = 2; //내 팔로워
                    }
                }
            }

            MemberDto memberDto = modelMapper.map(randomItem, MemberDto.class);
            memberDto.setIsFollowedCd(isFollowedCd);


            randomMemberList.add(memberDto);
        }
        return randomMemberList;
    }

}
