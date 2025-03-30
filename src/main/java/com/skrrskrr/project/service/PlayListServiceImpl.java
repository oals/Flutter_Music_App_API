package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.PlayListSelectQueryBuilder;
import com.skrrskrr.project.repository.PlayListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
@SuppressWarnings("unchecked")
public class PlayListServiceImpl implements PlayListService {


    @PersistenceContext
    EntityManager entitiyManager;
    
    private final JPAQueryFactory jpaQueryFactory;
    private final PlayListRepository playListRepository;
    private final ModelMapper modelMapper;


    @Override
    public Map<String,Object> getPlayList(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

            List<PlayListDto> playListDtoList = (List<PlayListDto>) playListSelectQueryBuilder
                    .selectFrom(QMemberPlayList.memberPlayList)
                    .findPlayListsByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .orderByPlayListIdDesc()
                    .limit(playListRequestDto.getLimit())
                    .offset(playListRequestDto.getOffset())
                    .fetchPlayListsDto(PlayListDto.class);


            Long totalCount = playListSelectQueryBuilder
                    .resetQuery()
                    .from(QMemberPlayList.memberPlayList)
                    .findPlayListsByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .fetchCount();


            if (playListRequestDto.getTrackId() != 0L) {
                playListDtoList = checkIsInPlayListTrack(playListRequestDto.getTrackId(), playListDtoList);
            }


            hashMap.put("playList",playListDtoList);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


    private List<PlayListDto> checkIsInPlayListTrack(Long trackId, List<PlayListDto> playListDtoList){

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        for (PlayListDto playListDto : playListDtoList) {

            /// 해당 플리에 추가하려는 트랙이 존재하는지 검사
            MemberPlayList memberPlayList = (MemberPlayList)
                    playListSelectQueryBuilder
                            .selectFrom(QMemberPlayList.memberPlayList)
                            .findPlayListsById(playListDto.getPlayListId())
                            .findIsInPlayListTrack(trackId)
                            .fetchFirst(MemberPlayList.class);

            playListDto.setIsInPlayList(memberPlayList != null);
        }

        return playListDtoList;
    }


    @Override
    public Map<String, Object> getPlayListInfo(PlayListRequestDto playListRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            PlayListDto playListDto = getPlayListById(playListRequestDto);
            Long trackCnt = playListDto.getTrackCnt();

            PlayListLike playListLike = selectPlayListLikeStatus(playListRequestDto);

            playListDto.setIsPlayListLike(isPlayListLike(playListLike));

            /* 임시 코드 */
            playListDto.setTotalPlayTime("7:77:77");

            hashMap.put("playList", playListDto);
            hashMap.put("totalCount", trackCnt);

            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    private PlayListDto getPlayListById(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        MemberPlayList memberPlayList = playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsById(playListRequestDto.getPlayListId())
                .fetchPlayListEntity(MemberPlayList.class);

        return playListEntityToDto(memberPlayList.getPlayList(), playListRequestDto);
    }


    private Boolean isPlayListLike(PlayListLike playListLike) {
        return playListLike != null && playListLike.getPlayListLikeStatus();
    }


    @Override
    public List<PlayListDto> getSearchPlayList(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .limit(searchRequestDto.getLimit())
                .offset(searchRequestDto.getOffset())
                .fetchPlayListPreviewDto(PlayListDto.class);
    }

    @Override
    public Long getSearchPlayListCnt(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .fetchCount();

    }

    @Override
    public Map<String,Object> setPlayListTrack(PlayListRequestDto playListRequestDto) {
        
        Map<String,Object> hashMap = new HashMap<>();
        QPlayList qPlayList = QPlayList.playList;
        QTrack qTrack = QTrack.track;

        try {
            PlayList playList = jpaQueryFactory.selectFrom(qPlayList)
                    .where(qPlayList.playListId.eq(playListRequestDto.getPlayListId()))
                    .fetchFirst();

            Track track = jpaQueryFactory.selectFrom(qTrack)
                    .where(qTrack.trackId.eq(playListRequestDto.getTrackId()))
                    .fetchFirst();

            if (playList != null && track != null) {

                if(playList.getPlayListTrackList().isEmpty()) {
                    playList.setPlayListImagePath(track.getTrackImagePath());
                }

                /** 트랙 시간 추가 코드 */


                playList.setTrackCnt(playList.getTrackCnt() + 1);
                playList.getPlayListTrackList().add(track);

                playListRepository.save(playList);
            }

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String,Object> setPlayListInfo(PlayListRequestDto playListRequestDto) {

        QPlayList qPlayList = QPlayList.playList;
        Map<String,Object> hashMap = new HashMap<>();

        try {

            jpaQueryFactory.update(qPlayList)
                    .set(qPlayList.playListNm, playListRequestDto.getPlayListNm())
                    .where(qPlayList.playListId.eq(playListRequestDto.getPlayListId()))
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();;
            hashMap.put("status","500");
        }
        return hashMap;
    }


    @Override
    public Map<String,Object> setPlayListLike(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            /// 해당 플리에 좋아요 기록이 있는지 조회
            PlayListLike playListLike = selectPlayListLikeStatus(playListRequestDto);

            /// 없므면 insert 추가'
            if (playListLike == null) {
                insertPlayListLike(playListRequestDto);
            } else {
                updatePlayListLike(playListRequestDto, playListLike);
            }

            hashMap.put("status","200");
        } catch (Exception e) {
           e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    private PlayListLike selectPlayListLikeStatus(PlayListRequestDto playListRequestDto){

        QPlayListLike qPlayListLike = QPlayListLike.playListLike;

        return jpaQueryFactory.selectFrom(qPlayListLike)
                .where(qPlayListLike.memberPlayList.playList.playListId.eq(playListRequestDto.getPlayListId())
                        .and(qPlayListLike.member.memberId.eq(playListRequestDto.getLoginMemberId())))
                .fetchFirst();
    }

    private void insertPlayListLike(PlayListRequestDto playListRequestDto) {

        MemberPlayList memberPlayList = getMemberPlayList(playListRequestDto);

        Member member = Member.builder()
                .memberId(playListRequestDto.getLoginMemberId())
                .build();

        insertPlayListLikeStatus(member,memberPlayList);


        insertPlayListLikeCnt(memberPlayList);

    }

    private void insertPlayListLikeStatus(Member member,MemberPlayList memberPlayList) {

        PlayListLike insertPlayListLike = new PlayListLike();
        insertPlayListLike.setMemberPlayList(memberPlayList);
        insertPlayListLike.setMember(member);
        insertPlayListLike.setPlayListLikeStatus(true);

        entitiyManager.persist(insertPlayListLike);
    }

    private void insertPlayListLikeCnt(MemberPlayList memberPlayList) {

        PlayList playList = memberPlayList.getPlayList();
        playList.setPlayListLikeCnt(playList.getPlayListLikeCnt() + 1);  // 좋아요 수 증가

        // 변경된 PlayList 엔티티를 merge하여 업데이트
        entitiyManager.merge(playList);  // 기존 엔티티 상태를 업데이트
    }

    private void updatePlayListLike(PlayListRequestDto playListRequestDto, PlayListLike playListLike) {

        Boolean trackLikeStatus = playListLike.getPlayListLikeStatus();

        MemberPlayList memberPlayList = getMemberPlayList(playListRequestDto);

        updatePlayListLikeStatus(memberPlayList,trackLikeStatus,playListRequestDto.getLoginMemberId());

        updatePlayListLikeCnt(memberPlayList,trackLikeStatus);

    }

    private void updatePlayListLikeStatus(MemberPlayList memberPlayList, Boolean trackLikeStatus,Long memberId){
        
        QPlayListLike qPlayListLike = QPlayListLike.playListLike;

        jpaQueryFactory.update(qPlayListLike)
                .set(qPlayListLike.playListLikeStatus, !trackLikeStatus)
                .where(qPlayListLike.memberPlayList.eq(memberPlayList)
                        .and(qPlayListLike.member.memberId.eq(memberId)))
                .execute();

    }

    private void updatePlayListLikeCnt(MemberPlayList memberPlayList, Boolean trackLikeStatus) {

        PlayList playList = memberPlayList.getPlayList();
        if (trackLikeStatus){
            playList.setPlayListLikeCnt(playList.getPlayListLikeCnt() - 1);  // 좋아요 수 증가
        } else {
            playList.setPlayListLikeCnt(playList.getPlayListLikeCnt() + 1);  // 좋아요 수 감소
        }

        // 변경된 PlayList 엔티티를 merge하여 업데이트
        entitiyManager.merge(playList);  // 기존 엔티티 상태를 업데이트
    }



    private MemberPlayList getMemberPlayList(PlayListRequestDto playListRequestDto) {

        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;

        return jpaQueryFactory.selectFrom(qMemberPlayList)
                .where(qMemberPlayList.playList.playListId.eq(playListRequestDto.getPlayListId()))
                .fetchFirst();
    }


    @Override
    public Map<String,Object> newPlayList(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            Member member = getMember(playListRequestDto.getLoginMemberId());
            assert member != null;

            PlayList playList = createPlayList(playListRequestDto,member);
            Long playListId = setPlayListRelationships(playList,member);

            hashMap.put("playListId",playListId);
            hashMap.put("status","200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
            return hashMap;
        }
    }


    public List<PlayListDto> getMemberPlayList(MemberRequestDto memberRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(memberRequestDto.getMemberId())
                .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(memberRequestDto.getLoginMemberId())
                .orderByPlayListIdDesc()
                .limit(memberRequestDto.getLimit())
                .offset(memberRequestDto.getOffset())
                .fetchPlayListPreviewDto(PlayListDto.class);
    }

    public Long getMemberPlayListCnt(MemberRequestDto memberRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(memberRequestDto.getMemberId())
                .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(memberRequestDto.getLoginMemberId())
                .fetchCount();
    }


    @Override
    public List<PlayListDto> getPopularPlayLists(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findIsPlayListNotEmpty()
                .findIsPlayListPrivacyFalse()
                .orderByPlayListLikeCntDesc()
                .limit(playListRequestDto.getLimit())
                .fetchPlayListPreviewDto(PlayListDto.class);
    }


    private Member getMember(Long memberId){

        QMember qMember = QMember.member;

        return jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(memberId))
                .fetchOne();
    }


    private PlayList createPlayList(PlayListRequestDto playListRequestDto, Member member) {

        // 플레이리스트 객체 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return PlayList.builder()
                .member(member)
                .playListNm(playListRequestDto.getPlayListNm())
                .isPlayListPrivacy(playListRequestDto.getIsPlayListPrivacy())
                .playListLikeCnt(0L)
                .isAlbum(playListRequestDto.getIsAlbum())
                .albumDate(playListRequestDto.getIsAlbum() ? LocalDateTime.now().format(formatter) : null)
                .memberPlayListList(new ArrayList<>()) // 관계 설정을 위한 리스트 초기화
                .build();

    }


    private Long setPlayListRelationships(PlayList playList, Member member){

        MemberPlayList memberPlayList = new MemberPlayList();

        memberPlayList.setPlayList(playList);
        memberPlayList.setMember(member);

        playList.getMemberPlayListList().add(memberPlayList);
        member.getMemberPlayListList().add(memberPlayList);

        return playListRepository.save(playList).getPlayListId();
    }


    private PlayListDto playListEntityToDto(PlayList playList, PlayListRequestDto playListRequestDto){

        PlayListDto playListDto = PlayListDto.builder()
                .playListId(playList.getPlayListId())
                .playListNm(playList.getPlayListNm())
                .trackCnt(playList.getTrackCnt())
                .playListImagePath(playList.getPlayListImagePath())
                .isPlayListPrivacy(playList.getIsPlayListPrivacy())
                .playListLikeCnt(playList.getPlayListLikeCnt())
                .trackCnt(playList.getTrackCnt())
                .albumDate(playList.getAlbumDate())
                .memberId(playList.getMember().getMemberId())
                .memberNickName(playList.getMember().getMemberNickName())
                .build();

        List<TrackDto> trackDtoList = new ArrayList<>();

        for (int i = playList.getPlayListTrackList().size() - 1; i >= 0; i--) {
            Track track = playList.getPlayListTrackList().get(i);

            if (track.getIsTrackPrivacy()) {
                if (!Objects.equals(track.getMemberTrackList().get(0).getMember().getMemberId(),
                        playListRequestDto.getLoginMemberId())) {
                    continue;
                }
            }

            TrackDto trackDto = modelMapper.map(track, TrackDto.class);
            trackDtoList.add(trackDto);
        }

        playListDto.setPlayListTrackList(trackDtoList);


        return playListDto;
    }






}
