package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.PlayListSelectQueryBuilder;
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
    private final TrackService trackService;
    private final ModelMapper modelMapper;


    @Override
    public Map<String, Object> getHomeInitPlayList(PlayListRequestDto playListRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();
        playListRequestDto.setLimit(6L);
        try {
            /// 인기 플리 추천  - 카테고리에 해당하는 곡의 수 , 조회수, 좋아요 수 ,
            List<PlayListDto> popularPlayList = getPopularPlayLists(playListRequestDto);
            hashMap.put("popularPlayList",popularPlayList);
            hashMap.put("status","200");
        } catch(Exception e){
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }

    @Override
    public Map<String, Object> getSearchPlayList(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        List<PlayListDto> playListDtoList = new ArrayList<>();

        try {

            /* 검색된 앨범, 플레이리스트 수 */
            Long playListCnt = getSearchPlayListCnt(searchRequestDto);

            if (playListCnt != 0L) {
                /* 검색된 앨범, 플레이리스트 정보 */
                playListDtoList = getSearchPlayLists(searchRequestDto);
            }
            hashMap.put("playListList", playListDtoList);   // 검색된 플레이리스트 리스트
            hashMap.put("playListListCnt", playListCnt);   // 전체 플레이리스트 수
            hashMap.put("status","200");
        }catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");

        }

        return hashMap;


    }

    @Override
    public Map<String, Object> getMemberPagePlayList(MemberRequestDto memberRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();

        try {

            // 플레이리스트 조회
            Long playListDtoListCnt = getMemberPlayListCnt(memberRequestDto);
            List<PlayListDto> playListDtoList = new ArrayList<>();
            if (playListDtoListCnt != 0L) {
                playListDtoList = getMemberPlayList(memberRequestDto);
            }


            hashMap.put("playListList", playListDtoList);
            hashMap.put("playListListCnt", playListDtoListCnt);
            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;


    }




    @Override
    public Map<String,Object> getPlayList(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

            List<PlayListDto> playListDtoList = (List<PlayListDto>) playListSelectQueryBuilder
                    .selectFrom(QMemberPlayList.memberPlayList)
                    .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                    .findPlayListsByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .orderByPlayListIdDesc()
                    .limit(playListRequestDto.getLimit())
                    .offset(playListRequestDto.getOffset())
                    .fetchPlayListPreviewDto(PlayListDto.class);


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
                            .findPlayListsByPlayListId(playListDto.getPlayListId())
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

            hashMap.put("playList", playListDto);
            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    private PlayListDto getPlayListById(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);


        return playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
                .findPlayListsByPlayListId(playListRequestDto.getPlayListId())
                .fetchPlayListDetailDto(PlayListDto.class);

    }


    @Override
    public List<PlayListDto> getSearchPlayLists(SearchRequestDto searchRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (List<PlayListDto>) playListSelectQueryBuilder
                .selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListBySearchText(searchRequestDto.getSearchText())
                .joinPlayListLikeWithMemberPlayList(searchRequestDto.getLoginMemberId())
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

        try {
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

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String,Object> setPlayListInfo(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            PlayListUpdateQueryBuilder playListUpdateQueryBuilder = new PlayListUpdateQueryBuilder(entitiyManager);

            playListUpdateQueryBuilder
                    .setEntity(QPlayList.playList)
                    .set(QPlayList.playList.playListNm, playListRequestDto.getPlayListNm())
                    .findPlayListByPlayListId(playListRequestDto.getPlayListId())
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();;
            hashMap.put("status","500");
        }
        return hashMap;
    }



    @Override
    public Map<String,Object> newPlayList(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            Member member = getMember(playListRequestDto.getLoginMemberId());

            if (member == null) {
                throw new IllegalStateException("member cannot be null.");
            }

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
                .joinPlayListLikeWithMemberPlayList(memberRequestDto.getLoginMemberId())
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
                .joinPlayListLikeWithMemberPlayList(playListRequestDto.getLoginMemberId())
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
            MemberTrack memberTrack = playList.getPlayListTrackList().get(i);

            if (memberTrack.getTrack().getIsTrackPrivacy()) {
                if (!Objects.equals(memberTrack.getTrack().getMemberTrackList().get(0).getMember().getMemberId(),
                        playListRequestDto.getLoginMemberId())) {
                    continue;
                }
            }

            TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
            trackDto.setMemberId(memberTrack.getMember().getMemberId());
            trackDto.setMemberNickName(memberTrack.getMember().getMemberNickName());
            trackDto.setTrackLikeStatus(false);
            trackDtoList.add(trackDto);
        }

        playListDto.setPlayListTrackList(trackDtoList);


        return playListDto;
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
