package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.MemberSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.MemberUpdateQueryBuilder;
import com.skrrskrr.project.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
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
    private final ModelMapper modelMapper;

    @Override
    public MemberDto getMemberInfo(HomeRequestDto homeRequestDto) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        MemberDto memberDto = memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberEmail(homeRequestDto.getMemberEmail())
                .fetchPreviewMemberDto(MemberDto.class);

        if (memberDto != null) {
            // 회원인 경우
            if (!Objects.equals(memberDto.getMemberDeviceToken(), homeRequestDto.getMemberDeviceToken())) {
                // 디바이스 토큰 업데이트
                homeRequestDto.setLoginMemberId(memberDto.getMemberId());
                Boolean isSuccess = setMemberDeviceToken(homeRequestDto);
                if (isSuccess) {
                    memberDto.setMemberDeviceToken(homeRequestDto.getMemberDeviceToken());
                }
            }
        } else {
            // 비회원인 경우
            memberDto = setMemberInfo(homeRequestDto);
        }

        return memberDto;
    }


    private Boolean setMemberDeviceToken(HomeRequestDto homeRequestDto) {

        try {
            MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

            memberUpdateQueryBuilder
                    .setEntity(QMember.member)
                    .set(QMember.member.memberDeviceToken, homeRequestDto.getMemberDeviceToken())
                    .findMemberByMemberId(homeRequestDto.getLoginMemberId())
                    .execute();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean setMemberImage(UploadDto uploadDto) {

        try {

            MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

            memberUpdateQueryBuilder
                    .setEntity(QMember.member)
                    .set(QMember.member.memberImagePath, uploadDto.getUploadImagePath())
                    .findMemberByMemberId(uploadDto.getLoginMemberId())
                    .execute();


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setMemberInfoUpdate(MemberRequestDto memberRequestDto) {

        QMember qMember = QMember.member;

        MemberUpdateQueryBuilder memberUpdateQueryBuilder = new MemberUpdateQueryBuilder(entityManager);

        memberUpdateQueryBuilder
                .setEntity(QMember.member)
                .set(QMember.member.memberNickName, memberRequestDto.getMemberNickName())
                .set(qMember.memberInfo,memberRequestDto.getMemberInfo())
                .findMemberByMemberId(memberRequestDto.getLoginMemberId())
                .execute();
    }

    private MemberDto setMemberInfo(HomeRequestDto homeRequestDto) {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        MemberDto memberDto = MemberDto.builder()
                .memberNickName(homeRequestDto.getMemberEmail().split("@")[0] + "_" + sb)
                .memberEmail(homeRequestDto.getMemberEmail())
                .memberDeviceToken(homeRequestDto.getMemberDeviceToken())
                .build();

        Member member = Member.createMember(memberDto);
        Member member1 = memberRepository.save(member);

        memberDto.setMemberId(member1.getMemberId());

        return memberDto;
    }


    @Override
    public List<FollowDto> getRecommendMember(Long loginMemberId) {

        MemberRequestDto memberRequestDto = new MemberRequestDto();
        memberRequestDto.setLoginMemberId(loginMemberId);
        memberRequestDto.setLimit(8L);

        return getRandomMemberList(memberRequestDto);
    }

    @Override
    public MemberResponseDto getSearchMember(SearchRequestDto searchRequestDto) {

        List<FollowDto> followMemberList = new ArrayList<>();

        Long totalCount = getSearchMemberListCnt(searchRequestDto);
        /* 검색된 멤버 정보*/

        if (totalCount != 0L) {
            followMemberList = getSearchMemberList(searchRequestDto);
        }

        return MemberResponseDto.builder()
                .followMemberList(followMemberList)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public Member getMemberEntity(Long memberId) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return (Member) memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberId(memberId)
                .fetchOne(Member.class);

    }


    @Override
    public MemberResponseDto getMemberPageInfo(MemberRequestDto memberRequestDto) {

        Member member = getMemberEntity(memberRequestDto.getMemberId());
        MemberDto memberDto = EntityToDto(member);

        return MemberResponseDto.builder()
                .member(memberDto)
                .build();
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

        return createRandomMmeberDtoList(queryMemberResult,searchRequestDto.getLoginMemberId());
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
    public List<FollowDto> getRandomMemberList(MemberRequestDto memberRequestDto){

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

    private List<FollowDto> createRandomMmeberDtoList(List<Member> queryResultMember, Long loginMemberId){

        List<FollowDto> memberDtos = new ArrayList<>();

        for (Member member : queryResultMember) {

            long isFollowedCd = 0; // 관계없음

            FollowDto followDto = FollowDto.builder()
                    .isFollowedCd(0L)
                    .followImagePath(member.getMemberImagePath())
                    .followMemberId(member.getMemberId())
                    .followNickName(member.getMemberNickName())
                    .isMutualFollow(false)
                    .build();

            for (int i = 0; i < member.getFollowing().size(); i++) {
                if (member.getFollowing().get(i).getFollower().getMemberId().equals(loginMemberId)) {
                    isFollowedCd = 1; // 내가 팔로우 중
                    break;
                }
            }

            for (int i = 0; i < member.getFollowers().size(); i++) {
                if (member.getFollowers().get(i).getFollowing().getMemberId().equals(loginMemberId)) {
                    if (isFollowedCd == 1) {
                        isFollowedCd = 3; //맞팔
                    } else {
                        isFollowedCd = 2; //내 팔로워
                    }
                }
            }

            followDto.setIsFollowedCd(isFollowedCd);
            memberDtos.add(followDto);
        }

        return memberDtos;




//        List<MemberDto> randomMemberList = new ArrayList<>();
//
//        for (Member randomItem : queryResultMember) {
//
//            int isFollowedCd = 0; // 관계없음
//
//            for (int i = 0; i < randomItem.getFollowing().size(); i++) {
//                if (randomItem.getFollowing().get(i).getFollower().getMemberId().equals(loginMemberId)) {
//                    isFollowedCd = 1; // 내가 팔로우 중
//                    break;
//                }
//            }
//
//            for (int i = 0; i < randomItem.getFollowers().size(); i++) {
//                if (randomItem.getFollowers().get(i).getFollowing().getMemberId().equals(loginMemberId)) {
//                    if (isFollowedCd == 1) {
//                        isFollowedCd = 3; //맞팔
//                    } else {
//                        isFollowedCd = 2; //내 팔로워
//                    }
//                }
//            }
//
//            MemberDto memberDto = modelMapper.map(randomItem, MemberDto.class);
//            memberDto.setIsFollowedCd(isFollowedCd);
//
//            randomMemberList.add(memberDto);
//        }
//            return randomMemberList;
    }

}
