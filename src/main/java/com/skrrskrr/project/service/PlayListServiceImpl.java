package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.PlayListSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.TrackSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.PlayListUpdateQueryBuilder;
import com.skrrskrr.project.repository.PlayListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final TrackLikeService trackLikeService;
    private final SearchService searchService;

    @Override
    public PlayListResponseDto getRecommendPlayList(PlayListRequestDto playListRequestDto) {

        playListRequestDto.setLimit(6L);
        playListRequestDto.setIsAlbum(false);

        List<PlayListDto> recommendPlayList = getRecommendPlayLists(playListRequestDto);

        return PlayListResponseDto.builder()
                .playLists(recommendPlayList)
                .build();
    }

    @Override
    public PlayListResponseDto getRecommendAlbum(PlayListRequestDto playListRequestDto) {

        playListRequestDto.setLimit(6L);
        playListRequestDto.setIsAlbum(true);

        List<PlayListDto> recommendAlbum = getRecommendPlayLists(playListRequestDto);

        return PlayListResponseDto.builder()
                .playLists(recommendAlbum)
                .build();
    }


    @Override
    public PlayListResponseDto getSearchPlayList(SearchRequestDto searchRequestDto) {

        List<PlayListDto> playListDtoList = new ArrayList<>();

        /* 검색된 앨범, 플레이리스트 수 */
        Long totalCount = getSearchPlayListCnt(searchRequestDto);

        if (totalCount != 0L) {
            /* 검색된 앨범, 플레이리스트 정보 */
            playListDtoList = getSearchPlayLists(searchRequestDto);
        }

        return PlayListResponseDto.builder()
                .playLists(playListDtoList)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public PlayListResponseDto getMemberPagePlayList(PlayListRequestDto playListRequestDto) {

        playListRequestDto.setIsAlbum(false);
        List<PlayListDto> playListDtoList = new ArrayList<>();

        Long totalCount = getMemberPlayListCnt(playListRequestDto);

        if (totalCount != 0L) {
            playListDtoList = getMemberPlayList(playListRequestDto);
        }

        return PlayListResponseDto.builder()
                .playLists(playListDtoList)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public PlayListResponseDto getMemberPageAlbums(PlayListRequestDto playListRequestDto) {

        playListRequestDto.setIsAlbum(true);
        List<PlayListDto> albumDtoList = new ArrayList<>();

        Long totalCount = getMemberPlayListCnt(playListRequestDto);

        if (totalCount != 0L) {
            albumDtoList = getMemberPlayList(playListRequestDto);
        }

        return PlayListResponseDto.builder()
                .playLists(albumDtoList)
                .totalCount(totalCount)
                .build();
    }

    private List<PlayListDto> getLikeTrackMemberPlayList(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(playListRequestDto.getMemberId())
                .findIsPlayListNotEmpty()
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .limit(playListRequestDto.getLimit())
                .offset(playListRequestDto.getOffset())
                .fetchPlayListDto(PlayListDto.class);
    }



    @Override
    public PlayListResponseDto getPlayList(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);
        List<PlayListDto> playListDtoList = new ArrayList<>();

        Long totalCount = playListSelectQueryBuilder
                .resetQuery()
                .from(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(playListRequestDto.getLoginMemberId())
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .fetchCount();

        if (totalCount != 0L) {
            playListDtoList = (List<PlayListDto>) playListSelectQueryBuilder
                    .selectFrom(QMemberPlayList.memberPlayList)
                    .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                    .findPlayListsByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .orderByPlayListIdDesc()
                    .limit(playListRequestDto.getLimit())
                    .offset(playListRequestDto.getOffset())
                    .fetchPlayListDto(PlayListDto.class);
        }

        if (playListRequestDto.getTrackId() != 0L) {
            playListDtoList = checkIsInPlayListTrack(playListRequestDto.getTrackId(), playListDtoList);
        }

        return PlayListResponseDto.builder()
                .playLists(playListDtoList)
                .totalCount(totalCount)
                .build();
    }

    private List<PlayListDto> checkIsInPlayListTrack(Long trackId, List<PlayListDto> playListDtoList){

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        for (PlayListDto playListDto : playListDtoList) {

            /// 해당 플리에 추가하려는 트랙이 존재하는지 검사
            MemberPlayList memberPlayList = (MemberPlayList)
                    playListSelectQueryBuilder
                            .selectFrom(QMemberPlayList.memberPlayList)
                            .findPlayListsByPlayListId(playListDto.getPlayListId())
                            .findIsInPlayListTrack(trackId)
                            .fetchFirst(MemberPlayList.class);

            playListDto.setIsInPlayList(memberPlayList != null);
        }

        return playListDtoList;
    }


    @Override
    public PlayListResponseDto getPlayListInfo(PlayListRequestDto playListRequestDto) {

        PlayListDto playListDto = getPlayListById(playListRequestDto);

        return PlayListResponseDto.builder()
                .playList(playListDto)
                .build();
    }


    private List<PlayListDto> getSearchKeywordPlayList(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .findPlayListBySearchTextList(searchRequestDto.getSearchTextList())
                .findPlayListsByNotMemberId(searchRequestDto.getLoginMemberId())
                .joinPlayListLikeWithMemberPlayList(searchRequestDto.getLoginMemberId())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .findIsAlbum(searchRequestDto.getIsAlbum())
                .limit(searchRequestDto.getLimit())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .offset(searchRequestDto.getOffset())
                .fetchPlayListDto(PlayListDto.class);


    }

    private PlayListDto getPlayListById(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);


        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                .findPlayListsByPlayListId(playListRequestDto.getPlayListId())
                .fetchPlayListDetailDto(PlayListDto.class);

    }

    private List<PlayListDto> getSearchPlayLists(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .findPlayListBySearchTextList(searchRequestDto.getSearchTextList())
                .joinPlayListLikeWithMemberPlayList(searchRequestDto.getLoginMemberId())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .findIsAlbum(searchRequestDto.getIsAlbum())
                .limit(searchRequestDto.getLimit())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .offset(searchRequestDto.getOffset())
                .fetchPlayListDto(PlayListDto.class);


    }

    private Long getSearchPlayListCnt(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .fetchCount();

    }

    @Override
    public void setPlayListTrack(PlayListRequestDto playListRequestDto) {

        PlayList playList = jpaQueryFactory.selectFrom(QPlayList.playList)
                .where(QPlayList.playList.playListId.eq(playListRequestDto.getPlayListId()))
                .fetchFirst();

        MemberTrack memberTrack = jpaQueryFactory.selectFrom(QMemberTrack.memberTrack)
                .where(QMemberTrack.memberTrack.track.trackId.eq(playListRequestDto.getTrackId()))
                .fetchFirst();

        if (playList != null && memberTrack != null) {

            if (playList.getPlayListImagePath() == null) {
                playList.setPlayListImagePath(memberTrack.getTrack().getTrackImagePath());
            }

            String playListTotalPlayTime = addTimes(memberTrack.getTrack().getTrackTime(),playList.getTotalPlayTime());

            playList.setTotalPlayTime(playListTotalPlayTime);
            playList.setTrackCnt(playList.getTrackCnt() + 1);
            playList.getPlayListTrackList().add(memberTrack);

            playListRepository.save(playList);
        }
    }

    @Override
    public void setPlayListInfo(PlayListRequestDto playListRequestDto) {

        PlayListUpdateQueryBuilder playListUpdateQueryBuilder = new PlayListUpdateQueryBuilder(entitiyManager);

        playListUpdateQueryBuilder
                .setEntity(QPlayList.playList)
                .set(QPlayList.playList.playListNm, playListRequestDto.getPlayListNm())
                .findPlayListByPlayListId(playListRequestDto.getPlayListId())
                .execute();
    }



    @Override
    public Long newPlayList(PlayListRequestDto playListRequestDto) {

        Member member = getMember(playListRequestDto.getLoginMemberId());

        if (member == null) {
            throw new IllegalStateException("member cannot be null.");
        }

        PlayList playList = createPlayList(playListRequestDto,member);

        return setPlayListRelationships(playList,member);
    }


    private List<PlayListDto> getMemberPlayList(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(playListRequestDto.getMemberId())
                .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .limit(playListRequestDto.getLimit())
                .offset(playListRequestDto.getOffset())
                .fetchPlayListDto(PlayListDto.class);
    }

    private Long getMemberPlayListCnt(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByMemberId(playListRequestDto.getMemberId())
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                .fetchCount();
    }

    private List<PlayListDto> getRecommendPlayLists(PlayListRequestDto playListRequestDto) {

        List<PlayListDto> recommendPlayList = new ArrayList<>();


        /** 최근 좋아요 누른 트랙의 사용자 최근 플리 조회 */
        TrackRequestDto trackRequestDto = new TrackRequestDto();
        trackRequestDto.setLoginMemberId(playListRequestDto.getLoginMemberId());
        trackRequestDto.setLimit(5L);

        List<Long> likeTrackMemberIdList = trackLikeService.getRecommendLikeTrackMemberId(trackRequestDto);

        playListRequestDto.setLimit(1L);

        for (Long memberId : likeTrackMemberIdList) {
            playListRequestDto.setMemberId(memberId);
            recommendPlayList.addAll(getLikeTrackMemberPlayList(playListRequestDto));
        }

        /** 팔로우한 유저의 최근 플리 */
        playListRequestDto.setLimit(5L);
        recommendPlayList.addAll(getFollowMemberPlayList(playListRequestDto));

        /** 검색어 기반 플리 조회*/
        SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setLoginMemberId(trackRequestDto.getLoginMemberId());
        searchRequestDto.setLimit(5L);
        searchRequestDto.setIsAlbum(playListRequestDto.getIsAlbum());

        searchRequestDto.setSearchTextList(searchService.processSearchKeywords(searchRequestDto));

        recommendPlayList.addAll(getSearchKeywordPlayList(searchRequestDto));

        recommendPlayList = recommendPlayList.stream()
                .distinct()
                .collect(Collectors.toList());

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        if (recommendPlayList.isEmpty()){

            recommendPlayList.addAll(
                    (List<PlayListDto>) playListSelectQueryBuilder
                            .selectFrom(QMemberPlayList.memberPlayList)
                            .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                            .findIsPlayListNotEmpty()
                            .findPlayListsByNotMemberId(playListRequestDto.getLoginMemberId())
                            .findIsPlayListPrivacyFalse()
                            .orderByPlayListLikeCntDesc()
                            .findIsAlbum(playListRequestDto.getIsAlbum())
                            .limit(playListRequestDto.getLimit())
                            .fetchPlayListPreviewDto(PlayListDto.class)
            );

        } else {
            Long addRecommendPlayListLimit = (long) (16 - recommendPlayList.size());

            List<Long> playListMemberIdList = recommendPlayList.stream()
                    .map(PlayListDto::getMemberId)
                    .toList();

            List<Long> playListIdList = recommendPlayList.stream()
                    .map(PlayListDto::getPlayListId)
                    .toList();

           recommendPlayList.addAll(
                    (List<PlayListDto>) playListSelectQueryBuilder
                            .selectFrom(QMemberPlayList.memberPlayList)
                            .findIsPlayListNotEmpty()
                            .findPlayListByMemberIdList(playListMemberIdList)
                            .findPlayListByPlayListIdList(playListIdList)
                            .findIsPlayListPrivacyFalse()
                            .orderByPlayListLikeCntDesc()
                            .findIsAlbum(playListRequestDto.getIsAlbum())
                            .limit(addRecommendPlayListLimit)
                            .fetchPlayListPreviewDto(PlayListDto.class)
            );

        }



        return recommendPlayList;
    }


    private Member getMember(Long memberId){

        QMember qMember = QMember.member;

        return jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(memberId))
                .fetchOne();
    }


    private List<PlayListDto> getFollowMemberPlayList(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder.selectFrom(QMemberPlayList.memberPlayList)
                .joinMemberPlayListWithMember()
                .joinMemberFollowersAndFollow()
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .findFollowerPlayLists(playListRequestDto.getLoginMemberId())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .limit(playListRequestDto.getLimit())
                .fetchPlayListPreviewDto(PlayListDto.class);

    }

    private PlayList createPlayList(PlayListRequestDto playListRequestDto, Member member) {

        // 플레이리스트 객체 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return PlayList.builder()
                .member(member)
                .playListNm(playListRequestDto.getPlayListNm())
                .isPlayListPrivacy(playListRequestDto.getIsPlayListPrivacy())
                .totalPlayTime("00:00")
                .playListLikeCnt(0L)
                .trackCnt(0L)
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


    private String addTimes(String trackTime, String playListTotalTime) {
        DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("H:mm:ss");
        DateTimeFormatter formatterWithoutHours = DateTimeFormatter.ofPattern("mm:ss");

        LocalTime time1 = parseTime(trackTime, formatterWithSeconds);
        LocalTime time2 = parseTime(playListTotalTime, formatterWithSeconds);

        LocalTime totalTime = time1.plusHours(time2.getHour())
                .plusMinutes(time2.getMinute())
                .plusSeconds(time2.getSecond());

        if (totalTime.getHour() == 0) {
            return totalTime.format(formatterWithoutHours);
        } else {
            return totalTime.format(formatterWithSeconds);
        }
    }




    private LocalTime parseTime(String time, DateTimeFormatter formatter) {

        if (time.length() == 5) {
            time = "00:" + time;
        }


        if (time.length() == 5 && time.indexOf(":") == 2) {
            time = "00:" + time;
        }

        return LocalTime.parse(time, formatter);
    }

}
