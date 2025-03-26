package com.skrrskrr.project.service;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final ModelMapper modelMapper;

    @Override
    public MemberDto getMemberInfo(MemberRequestDto memberRequestDto) {

        
        QMember qmember = QMember.member; // QMember 타입 사용

        // QueryDSL을 사용하여 authToken으로 Member 조회
        Member foundMember = jpaQueryFactory
                .selectFrom(qmember)
                .where(qmember.memberEmail.eq(memberRequestDto.getMemberEmail()))
                .fetchOne(); // 단일 결과를 가져옴

        if (foundMember != null) {
            return EntityToDto(foundMember);
        } else {
            return null;
        }

    }


    @Override
    public Map<String,Object> setMemberDeviceToken(MemberRequestDto memberRequestDto) {
        
        QMember qMember = QMember.member;
        Map<String,Object> hashMap = new HashMap<>();
        try {
            jpaQueryFactory.update(qMember)
                    .set(qMember.memberDeviceToken, memberRequestDto.getDeviceToken())
                    .where(qMember.memberId.eq(memberRequestDto.getLoginMemberId()))
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

            QMember qMember = QMember.member;

            jpaQueryFactory.update(qMember)
                    .set(qMember.memberImagePath, uploadDto.getUploadImagePath())
                    .where(qMember.memberId.eq(uploadDto.getLoginMemberId()))
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
            // 동적으로 memberNickName과 memberInfo를 설정하는 방식
            jpaQueryFactory.update(qMember)
                    .set(qMember.memberNickName,
                            memberRequestDto.getMemberNickName() != null ?
                                    Expressions.constant(memberRequestDto.getMemberNickName()) : qMember.memberNickName)
                    .set(qMember.memberInfo,
                            memberRequestDto.getMemberNickName() == null ?
                                    Expressions.constant(memberRequestDto.getMemberInfo()) : qMember.memberInfo)
                    .where(qMember.memberId.eq(memberRequestDto.getLoginMemberId()))
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
    public Map<String, Object> getMemberPageInfo(MemberRequestDto memberRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        MemberDto memberDto;

        QMember qMember = QMember.member;
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        try {

            MemberTrack queryResultMemberTrack = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(qMemberTrack.member.memberId.eq(memberRequestDto.getMemberId()))
                    .fetchFirst();

            // 회원 정보 조회
            if (queryResultMemberTrack == null) {
                Member member = jpaQueryFactory.selectFrom(qMember)
                        .where(qMember.memberId.eq(memberRequestDto.getMemberId()))
                        .fetchFirst();
                memberDto = EntityToDto(member);
            } else {
                Member member = queryResultMemberTrack.getMember();
                memberDto = EntityToDto(member);
            }

            // 플레이리스트 조회
            Long playListDtoListCnt = getMemberPlayListCnt(memberRequestDto);
            List<PlayListDto> playListDtoList = new ArrayList<>();
            if(playListDtoListCnt != 0L) {
                playListDtoList = getMemberPlayList(memberRequestDto,0L,5L);
            }

            List<TrackDto> allTrackDtoList = new ArrayList<>();
            List<TrackDto> popularTrackDtoList = new ArrayList<>();
            Long allTrackDtoListCnt = getMemberTrackListCnt(memberRequestDto,false);
            if (allTrackDtoListCnt != 0){
                allTrackDtoList = getMemberTrack(memberRequestDto,false,0L,7L);
                popularTrackDtoList = getMemberTrack(memberRequestDto, true,0L,4L);
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



    public List<TrackDto> getMemberTrack(MemberRequestDto memberRequestDto, boolean isPopular , Long offset, Long limit){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        // 인기 트랙 및 모든 트랙 조회
            List<MemberTrack> trackList = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(qMemberTrack.member.memberId.eq(memberRequestDto.getMemberId())
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse()
                                    .or(qMemberTrack.member.memberId.eq(memberRequestDto.getLoginMemberId()))))
                    .orderBy(isPopular ? qMemberTrack.track.trackPlayCnt.desc() : qMemberTrack.track.trackId.desc())
                    .limit(limit)
                    .offset(offset)
                    .fetch();


            List<TrackDto> trackDtoList = new ArrayList<>();
            for (MemberTrack memberTrack : trackList) {
                TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
                trackDtoList.add(trackDto);
            }


        return trackDtoList;

    }

    public Long getMemberTrackListCnt(MemberRequestDto memberRequestDto, boolean isPopular) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.select(
                        qMemberTrack.count()
                ).from(qMemberTrack)
                .where(qMemberTrack.member.memberId.eq(memberRequestDto.getMemberId())
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse()
                                .or(qMemberTrack.member.memberId.eq(memberRequestDto.getLoginMemberId()))))
                .orderBy(isPopular ? qMemberTrack.track.trackPlayCnt.desc() : qMemberTrack.track.trackId.desc())
                .fetchOne();

    }

    public List<PlayListDto> getMemberPlayList(MemberRequestDto memberRequestDto, Long offset, Long limit) {

        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;
        List<PlayListDto> playListDtoList = new ArrayList<>();

        List<MemberPlayList> memberPlayList = jpaQueryFactory
                .selectFrom(qMemberPlayList)
                .where(qMemberPlayList.member.memberId.eq(memberRequestDto.getMemberId())
                        .and(qMemberPlayList.playList.isPlayListPrivacy.isFalse()
                                .or(qMemberPlayList.member.memberId.eq(memberRequestDto.getLoginMemberId()))))
                .limit(limit)
                .offset(offset)
                .fetch();


        for (MemberPlayList playList : memberPlayList) {
            String playListFirstTrackImagePath = !playList.getPlayList().getPlayListTrackList().isEmpty()
                    ? playList.getPlayList().getPlayListTrackList().get(0).getTrackImagePath()
                    : "";

            PlayListDto playListDTO = PlayListDto.builder()
                    .playListId(playList.getPlayList().getPlayListId())
                    .playListNm(playList.getPlayList().getPlayListNm())
                    .playListImagePath(playListFirstTrackImagePath)
                    .memberNickName(playList.getMember().getMemberNickName())
                    .memberId(playList.getPlayList().getMember().getMemberId())
                    .build();

            playListDtoList.add(playListDTO);
        }

        return playListDtoList;
    }

    public Long getMemberPlayListCnt(MemberRequestDto memberRequestDto) {

        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;

        return jpaQueryFactory
                .select(qMemberPlayList.count())  // count()로 개수를 구함
                .from(qMemberPlayList)
                .where(qMemberPlayList.member.memberId.eq(memberRequestDto.getMemberId())
                        .and(qMemberPlayList.playList.isPlayListPrivacy.isFalse()
                                .or(qMemberPlayList.member.memberId.eq(memberRequestDto.getLoginMemberId()))))
                .fetchOne();  // 결과값을 Long으로 반환
    }


    public List<MemberDto> getRandomMemberList(MemberRequestDto memberRequestDto){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        List<MemberTrack> queryResultMember = jpaQueryFactory.selectFrom(qMemberTrack)
                .where(
                        qMemberTrack.member.memberId.ne(memberRequestDto.getLoginMemberId()) // 자기 자신 제외
                                .and(qMemberTrack.member.memberTrackList.isNotEmpty())  // 트랙 리스트가 비어 있지 않음
                )
                .groupBy(qMemberTrack.member.memberId) // 멤버 아이디별로 그룹화
                .orderBy(
                        Expressions.numberTemplate(Double.class, "function('RAND')").asc()  // 랜덤 정렬
                )
                .limit(memberRequestDto.getLimit())  // 랜덤으로 8개만 추출
                .fetch();



        return createRandomMmeberDtoList(queryResultMember, memberRequestDto.getLoginMemberId());
    }

    private List<MemberDto> createRandomMmeberDtoList(List<MemberTrack> queryResultMember, Long loginMemberId){

        List<MemberDto> randomMemberList = new ArrayList<>();

        for (MemberTrack randomItem : queryResultMember) {

            int isFollowedCd = 0; // 관계없음

            for (int i = 0; i < randomItem.getMember().getFollowers().size(); i++) {
                if (randomItem.getMember().getFollowers().get(i).getFollowing().getMemberId().equals(loginMemberId)) {
                    isFollowedCd = 1; // 내가 팔로우 중
                    break;
                }
            }

            for (int i = 0; i < randomItem.getMember().getFollowing().size(); i++) {
                if (randomItem.getMember().getFollowing().get(i).getFollower().getMemberId().equals(loginMemberId)) {
                    if(isFollowedCd == 1){
                        isFollowedCd = 3; //맞팔
                    } else {
                        isFollowedCd = 2; //내 팔로워
                    }
                }
            }

            MemberDto memberDto = modelMapper.map(randomItem.getMember(), MemberDto.class);
            memberDto.setIsFollowedCd(isFollowedCd);


            randomMemberList.add(memberDto);
        }
        return randomMemberList;
    }

}
