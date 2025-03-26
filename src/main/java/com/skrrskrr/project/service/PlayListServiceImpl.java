package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.entity.*;
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
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
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
            List<PlayList> queryResult = getPlayListList(playListRequestDto);

            List<PlayListDto> list = new ArrayList<>();
            for (PlayList playList : queryResult) {

                PlayListDto playListDto = modelMapper.map(playList, PlayListDto.class);
                playListDto.setMemberId(playList.getMember().getMemberId());
                playListDto.setMemberNickName(playList.getMember().getMemberNickName());

                /// 해당 플리에 추가하려는 트랙이 존재하는지 검사
                if (playListRequestDto.getTrackId() != 0L) {
                    playListDto = checkIsInPlayListTrack(playListRequestDto.getTrackId(),playList,playListDto);
                }

                list.add(playListDto);
            }

            int totalCount = getPlayListListCnt(playListRequestDto);


            hashMap.put("playList",list);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


    private List<PlayList> getPlayListList(PlayListRequestDto playListRequestDto) {

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory.selectFrom(qPlayList)
                .where(qPlayList.member.memberId.eq(playListRequestDto.getLoginMemberId())
                        .and(qPlayList.isAlbum.eq(playListRequestDto.isAlbum())))
                .orderBy(qPlayList.playListId.desc())
                .offset(playListRequestDto.getOffset())
                .limit(playListRequestDto.getLimit())
                .fetch();
    }

    private int getPlayListListCnt(PlayListRequestDto playListRequestDto){

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory
                .selectFrom(qPlayList)
                .where(qPlayList.member.memberId.eq(playListRequestDto.getLoginMemberId())
                        .and(qPlayList.isAlbum.eq(playListRequestDto.isAlbum()))
                )
                .fetch()
                .size();
    }


    private PlayListDto checkIsInPlayListTrack(Long trackId, PlayList playList, PlayListDto playListDto){

        playListDto.setIsInPlayList(false);
        if (!playList.getPlayListTrackList().isEmpty()) {
            for (Track track : playList.getPlayListTrackList()) {
                if(trackId != 0) {
                    if (Objects.equals(track.getTrackId(), trackId)) {
                        playListDto.setIsInPlayList(true);
                    }
                }
            }

            playListDto.setTrackCnt((long) playList.getPlayListTrackList().size());
            playListDto.setPlayListImagePath(playList.getPlayListTrackList().get(0).getTrackImagePath());
        }
        return playListDto;
    }



    @Override
    public Map<String, Object> getPlayListInfo(PlayListRequestDto playListRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            PlayList playList = fetchPlayListById(playListRequestDto.getPlayListId());
            assert playList != null;

            PlayListLike playListLike = selectPlayListLikeStatus(playListRequestDto);
            boolean isPlayListLikeStatus = isPlayListLike(playListLike);

            PlayListDto playListDto = modelMapper.map(playList, PlayListDto.class);
            playListDto.setMemberId(playList.getMember().getMemberId());
            playListDto.setMemberNickName(playList.getMember().getMemberNickName());
            playListDto.setPlayListLike(isPlayListLikeStatus);

            int totalCount = playList.getPlayListTrackList().size();

            if (totalCount != 0) {
                List<TrackDto> trackDtoList = createTrackDtoList(playList, playListRequestDto);

                playListDto.setPlayListTrackList(trackDtoList);
                playListDto.setPlayListImagePath(trackDtoList.get(0).getTrackImagePath());

                int[] totalPlayTime = calculateTotalPlayTime(trackDtoList);

                playListDto.setTotalPlayTime(String.format("%d:%02d:%02d", totalPlayTime[0], totalPlayTime[1], totalPlayTime[2]));
                playListDto.setTrackCnt((long) trackDtoList.size());
            }

            hashMap.put("playList", playListDto);
            hashMap.put("totalCount", totalCount);

            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    private PlayList fetchPlayListById(Long playListId) {
        
        QPlayList qPlayList = QPlayList.playList;
        return jpaQueryFactory.selectFrom(qPlayList)
                .where(qPlayList.playListId.eq(playListId))
                .fetchOne();
    }

    private boolean isPlayListLike(PlayListLike playListLike) {
        return playListLike != null && playListLike.isPlayListLikeStatus();
    }


    private List<TrackDto> createTrackDtoList(PlayList playList, PlayListRequestDto playListRequestDto) {

        List<TrackDto> trackDtoList = new ArrayList<>();

        List<Track> trackList = playList.getPlayListTrackList()
                .stream()
                .skip(playListRequestDto.getOffset())
                .limit(playListRequestDto.getLimit())      // limit 개수만큼만 선택
                .toList();

        if (!trackList.isEmpty()) {
            for (int i = trackList.size() - 1; i >= 0; i--) {
                if (trackList.get(i).isTrackPrivacy()) {
                    if (!Objects.equals(playList.getMember().getMemberId(), playListRequestDto.getLoginMemberId())) {
                        continue;
                    }
                }

                TrackDto trackDto = modelMapper.map(trackList.get(i), TrackDto.class);
                trackDtoList.add(trackDto);
            }
        }
        return trackDtoList;
    }


    private int[] calculateTotalPlayTime(List<TrackDto> trackDtoList) {
        int totalMinutes = 0;
        int totalSeconds = 0;

        for (TrackDto trackDto : trackDtoList) {
            String[] parts = trackDto.getTrackTime().split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            totalMinutes += minutes;
            totalSeconds += seconds;
        }

        totalMinutes += totalSeconds / 60;
        totalSeconds = totalSeconds % 60;

        int totalHours = totalMinutes / 60;
        totalMinutes = totalMinutes % 60;

        return new int[]{totalHours, totalMinutes, totalSeconds};
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

        boolean trackLikeStatus = playListLike.isPlayListLikeStatus();

        MemberPlayList memberPlayList = getMemberPlayList(playListRequestDto);

        updatePlayListLikeStatus(memberPlayList,trackLikeStatus,playListRequestDto.getLoginMemberId());

        updatePlayListLikeCnt(memberPlayList,trackLikeStatus);

    }


    private void updatePlayListLikeStatus(MemberPlayList memberPlayList, boolean trackLikeStatus,Long memberId){
        
        QPlayListLike qPlayListLike = QPlayListLike.playListLike;

        jpaQueryFactory.update(qPlayListLike)
                .set(qPlayListLike.playListLikeStatus, !trackLikeStatus)
                .where(qPlayListLike.memberPlayList.eq(memberPlayList)
                        .and(qPlayListLike.member.memberId.eq(memberId)))
                .execute();

    }


    private void updatePlayListLikeCnt(MemberPlayList memberPlayList, boolean trackLikeStatus) {

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

    @Override
    public List<PlayListDto> getPopularPlayList(PlayListRequestDto playListRequestDto) {


        QPlayList qPlayList = QPlayList.playList;
        QTrack qTrack = QTrack.track;

        List<PlayList> queryResultPlayList =  jpaQueryFactory.selectFrom(qPlayList)
                .join(qPlayList.playListTrackList, qTrack)
                .groupBy(qPlayList.playListId)  // PlayList 단위로 그룹화
                .orderBy(
                        qTrack.trackPlayCnt.count().desc(),      // trackPlayCnt의 등장 횟수
                        qTrack.trackLikeCnt.count().desc()
                )
                .where(qPlayList.playListTrackList.isNotEmpty()
                        .and(qPlayList.isPlayListPrivacy.isFalse()))  // Track 리스트가 비어 있지 않은 PlayList만
                .limit(playListRequestDto.getLimit())
                .fetch();


        List<PlayListDto> popularPlayList = new ArrayList<>();
        for (PlayList playList : queryResultPlayList) {
            PlayListDto playListDto = PlayListDto.builder()
                    .playListId(playList.getPlayListId())
                    .playListNm(playList.getPlayListNm())
                    .playListImagePath(playList.getPlayListTrackList().get(playList.getPlayListTrackList().size() - 1).getTrackImagePath())
                    .memberNickName(playList.getMember().getMemberNickName())
                    .build();
            popularPlayList.add(playListDto);


        }
        return popularPlayList;
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
                .isAlbum(playListRequestDto.isAlbum())
                .albumDate(playListRequestDto.isAlbum() ? LocalDateTime.now().format(formatter) : null)
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


}
