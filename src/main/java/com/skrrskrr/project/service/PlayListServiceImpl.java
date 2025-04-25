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
    private final PlayListLikeService playListLikeService;
    private final SearchService searchService;

    @Override
    public List<PlayListDto> getRecommendPlayList(Long loginMemberId) {

        PlayListRequestDto playListRequestDto = new PlayListRequestDto();
        playListRequestDto.setIsAlbum(false);
        playListRequestDto.setLoginMemberId(loginMemberId);

        return getRecommendPlayLists(playListRequestDto);
    }

    @Override
    public List<PlayListDto> getRecommendAlbum(Long loginMemberId) {

        PlayListRequestDto playListRequestDto = new PlayListRequestDto();
        playListRequestDto.setIsAlbum(true);
        playListRequestDto.setLoginMemberId(loginMemberId);

        List<PlayListDto> recommendAlbum = getRecommendPlayLists(playListRequestDto);

        return recommendAlbum;
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

    private List<PlayListDto> getLikeTrackMemberPopularPlayList(PlayListRequestDto playListRequestDto) {

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
                .fetchPlayListPreviewDto(PlayListDto.class);
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
                .findPlayListBySearchTextList(searchRequestDto.getSearchKeywordList())
                .findPlayListsByNotMemberId(searchRequestDto.getLoginMemberId())
                .joinPlayListLikeWithMemberPlayList(searchRequestDto.getLoginMemberId())
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .findIsAlbum(searchRequestDto.getIsAlbum())
                .limit(searchRequestDto.getLimit())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .offset(searchRequestDto.getOffset())
                .fetchPlayListPreviewDto(PlayListDto.class);


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
                .findPlayListBySearchTextList(searchRequestDto.getSearchKeywordList())
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


    private List<PlayListDto> getRecommendLikeMemberPopularPlayList(PlayListRequestDto playListRequestDto) {

        List<PlayListDto> recommendPlaysList = new ArrayList<>();
        List<Long> likeTrackMemberIdList = playListLikeService.getRecommendLikePlayListsMemberId(playListRequestDto);

        playListRequestDto.setLimit(1L);

        for (Long memberId : likeTrackMemberIdList) {
            playListRequestDto.setMemberId(memberId);
            recommendPlaysList.addAll(getLikeTrackMemberPopularPlayList(playListRequestDto));
        }

        return recommendPlaysList;

    }

    private List<PlayListDto> getSearchRecommendPlayLists(PlayListRequestDto playListRequestDto) {
        SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setLoginMemberId(playListRequestDto.getLoginMemberId());
        searchRequestDto.setLimit(5L);
        searchRequestDto.setIsAlbum(playListRequestDto.getIsAlbum());

        searchRequestDto.setSearchKeywordList(searchService.processSearchKeywords(searchRequestDto));

        return getSearchKeywordPlayList(searchRequestDto);
    }

    private List<PlayListDto> getRecommendPopularPlayLists(PlayListRequestDto playListRequestDto){

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findIsPlayListNotEmpty()
                .findPlayListsByNotMemberId(playListRequestDto.getLoginMemberId())
                .findIsPlayListPrivacyFalse()
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .orderByPlayListLikeCntDesc()
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .limit(15L)
                .fetchPlayListPreviewDto(PlayListDto.class);
    }

    private List<Long> getRecommendPlayListsPlayListId(List<PlayListDto> recommendPlayList){
        return recommendPlayList.stream()
                .map(PlayListDto::getPlayListId)
                .toList();
    }
    private List<Long> getRecommendPlayListsMemberIdList(List<PlayListDto> recommendPlayList){
        return recommendPlayList.stream()
                .map(PlayListDto::getMemberId)
                .toList();
    }

    private List<PlayListDto> getMemberRecommendPlayList(PlayListRequestDto playListRequestDto,List<Long> playListMemberIdList,List<Long> playListIdList, Long limit){

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListByNotPlayListIdList(playListIdList)
                .findPlayListByMemberIdList(playListMemberIdList)
                .findIsPlayListPrivacyFalse()
                .findIsPlayListNotEmpty()
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .orderByPlayListLikeCntDesc()
                .limit(limit)
                .fetchPlayListPreviewDto(PlayListDto.class);
    }

    private List<PlayListDto> getRecommendPlayLists(PlayListRequestDto playListRequestDto) {

        List<PlayListDto> recommendPlayList = new ArrayList<>();

        /* 최근 좋아요 누른 플레이리스트 사용자의 플레이리스트 조회 */
        recommendPlayList.addAll(getRecommendLikeMemberPopularPlayList(playListRequestDto));

        /* 팔로우한 유저의 최근 플리 */
        recommendPlayList.addAll(getFollowMemberPlayList(playListRequestDto));

        /* 사용자의 검색 키워드가 포함 되는 플레이리스트 조회 */
        recommendPlayList.addAll(getSearchRecommendPlayLists(playListRequestDto));

        /*중복 제거*/
        recommendPlayList = new ArrayList<>(recommendPlayList.stream()
                .collect(Collectors.toMap(
                        PlayListDto::getPlayListId,
                        item -> item,
                        (existing, replacement) -> existing
                ))
                .values());

        if (recommendPlayList.isEmpty()){

            recommendPlayList.addAll(getRecommendPopularPlayLists(playListRequestDto));

        } else if (recommendPlayList.size() < 15) {

            Long addRecommendPlayListLimit = (long) (15 - recommendPlayList.size());

            List<Long> playListMemberIdList = getRecommendPlayListsMemberIdList(recommendPlayList);

            List<Long> playListIdList = getRecommendPlayListsPlayListId(recommendPlayList);

            recommendPlayList.addAll(getMemberRecommendPlayList(playListRequestDto,playListMemberIdList,playListIdList,addRecommendPlayListLimit));

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
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .findFollowerPlayLists(playListRequestDto.getLoginMemberId())
                .orderByPlayListLikeCntDesc()
                .orderByPlayListIdDesc()
                .limit(5L)
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
