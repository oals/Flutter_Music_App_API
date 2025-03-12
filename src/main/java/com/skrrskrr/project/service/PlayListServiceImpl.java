package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.TrackDTO;
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
    public Map<String,Object> getPlayList(Long memberId, Long trackId , Long listIndex,boolean isAlbum) {


        Map<String,Object> hashMap = new HashMap<>();

        try {
            List<PlayList> queryResult = getPlayList(memberId,isAlbum,listIndex);


            List<PlayListDTO> list = new ArrayList<>();

            for (PlayList playList : queryResult) {

                PlayListDTO playListDTO = modelMapper.map(playList,PlayListDTO.class);
                playListDTO.setMemberId(playList.getMember().getMemberId());
                playListDTO.setMemberNickName(playList.getMember().getMemberNickName());

                /// 해당 플리에 추가하려는 트랙이 존재하는지 검사
                playListDTO = checkIsInPlayListTrack(trackId,playList,playListDTO);

                list.add(playListDTO);
            }

            int totalCount = getPlayListCnt(memberId,isAlbum);


            hashMap.put("playList",list);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


    private List<PlayList> getPlayList(Long memberId, boolean isAlbum, Long listIndex) {

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory.selectFrom(qPlayList)
                .where(qPlayList.member.memberId.eq(memberId)
                        .and(qPlayList.isAlbum.eq(isAlbum)))
                .orderBy(qPlayList.playListId.desc())
                .offset(listIndex)
                .limit(20)
                .fetch();
    }

    private int getPlayListCnt(Long memberId, boolean isAlbum){

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory
                .selectFrom(qPlayList)
                .where(qPlayList.member.memberId.eq(memberId)
                        .and(qPlayList.isAlbum.eq(isAlbum))
                )
                .fetch()
                .size();
    }


    private PlayListDTO checkIsInPlayListTrack(Long trackId,PlayList playList, PlayListDTO playListDTO){

        playListDTO.setIsInPlayList(false);
        if (!playList.getPlayListTrackList().isEmpty()) {
            for (Track track : playList.getPlayListTrackList()) {
                if(trackId != 0) {
                    if (Objects.equals(track.getTrackId(), trackId)) {
                        playListDTO.setIsInPlayList(true);
                    }
                }
            }

            playListDTO.setTrackCnt((long) playList.getPlayListTrackList().size());
            playListDTO.setPlayListImagePath(playList.getPlayListTrackList().get(0).getTrackImagePath());
        }
        return playListDTO;
    }



    @Override
    public Map<String, Object> getPlayListInfo(PlayListDTO playListDTO) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            PlayList playList = fetchPlayListById(playListDTO.getPlayListId());
            assert playList != null;

            PlayListLike playListLike = selectPlayListLikeStatus(playListDTO);
            boolean isPlayListLikeStatus = isPlayListLike(playListLike);

            PlayListDTO playListListDTO = modelMapper.map(playList,PlayListDTO.class);
            playListListDTO.setMemberId(playList.getMember().getMemberId());
            playListListDTO.setMemberNickName(playList.getMember().getMemberNickName());
            playListListDTO.setPlayListLike(isPlayListLikeStatus);


            List<TrackDTO> trackDTOList = createTrackDTOList(playList, playListDTO);

            playListListDTO.setPlayListTrackList(trackDTOList);

            int[] totalPlayTime = calculateTotalPlayTime(trackDTOList);

            playListListDTO.setTotalPlayTime(String.format("%d:%02d:%02d", totalPlayTime[0], totalPlayTime[1], totalPlayTime[2]));
            playListListDTO.setTrackCnt((long) trackDTOList.size());

            hashMap.put("playList", playListListDTO);
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


    private List<TrackDTO> createTrackDTOList(PlayList queryResult, PlayListDTO playListDTO) {
        List<TrackDTO> trackDTOList = new ArrayList<>();
        List<Track> trackList = queryResult.getPlayListTrackList();

        if (!trackList.isEmpty()) {
            for (int i = trackList.size() - 1; i >= 0; i--) {
                if (trackList.get(i).isTrackPrivacy()) {
                    if (!Objects.equals(queryResult.getMember().getMemberId(), playListDTO.getMemberId())) {
                        continue;
                    }
                }

                TrackDTO trackDTO = modelMapper.map(trackList.get(i), TrackDTO.class);
                trackDTOList.add(trackDTO);
            }
        }
        return trackDTOList;
    }


    private int[] calculateTotalPlayTime(List<TrackDTO> trackDTOList) {
        int totalMinutes = 0;
        int totalSeconds = 0;

        for (TrackDTO trackDTO : trackDTOList) {
            String[] parts = trackDTO.getTrackTime().split(":");
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
    public Map<String,Object> setPlayListTrack(PlayListDTO playListDTO) {
        
        Map<String,Object> hashMap = new HashMap<>();
        QPlayList qPlayList = QPlayList.playList;
        QTrack qTrack = QTrack.track;

        try {
            PlayList playList = jpaQueryFactory.selectFrom(qPlayList)
                    .where(qPlayList.playListId.eq(playListDTO.getPlayListId()))
                    .fetchFirst();

            Track track = jpaQueryFactory.selectFrom(qTrack)
                    .where(qTrack.trackId.eq(playListDTO.getTrackId()))
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
    public Map<String,Object> setPlayListInfo(PlayListDTO playListDTO) {

        QPlayList qPlayList = QPlayList.playList;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            jpaQueryFactory.update(qPlayList)
                    .set(qPlayList.playListNm, playListDTO.getPlayListNm())
                    .where(qPlayList.playListId.eq(playListDTO.getPlayListId()))
                    .execute();
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();;
            hashMap.put("status","500");
        }
        return hashMap;
    }


    @Override
    public Map<String,Object> setPlayListLike(PlayListDTO playListDTO) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            /// 해당 플리에 좋아요 기록이 있는지 조회
            PlayListLike playListLike = selectPlayListLikeStatus(playListDTO);

            /// 없므면 insert 추가'
            if (playListLike == null) {
                insertPlayListLike(playListDTO);
            } else {
                updatePlayListLike(playListDTO, playListLike);
            }

            hashMap.put("status","200");
        } catch (Exception e) {
           e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    private PlayListLike selectPlayListLikeStatus(PlayListDTO playListDTO){

        QPlayListLike qPlayListLike = QPlayListLike.playListLike;

        return jpaQueryFactory.selectFrom(qPlayListLike)
                .where(qPlayListLike.memberPlayList.playList.playListId.eq(playListDTO.getPlayListId())
                        .and(qPlayListLike.member.memberId.eq(playListDTO.getMemberId())))
                .fetchFirst();
    }



    private void insertPlayListLike(PlayListDTO playListDTO) {

        MemberPlayList memberPlayList = getMemberPlayList(playListDTO.getPlayListId());

        Member member = Member.builder()
                .memberId(playListDTO.getMemberId())
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



    private void updatePlayListLike(PlayListDTO playListDTO, PlayListLike playListLike) {

        boolean trackLikeStatus = playListLike.isPlayListLikeStatus();

        MemberPlayList memberPlayList = getMemberPlayList(playListDTO.getPlayListId());

        updatePlayListLikeStatus(memberPlayList,trackLikeStatus,playListDTO.getMemberId());

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


    private MemberPlayList getMemberPlayList(Long playListId) {

        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;

        return jpaQueryFactory.selectFrom(qMemberPlayList)
                .where(qMemberPlayList.playList.playListId.eq(playListId))
                .fetchFirst();
    }


    @Override
    public Map<String,Object> newPlayList(PlayListDTO playListDTO) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            Member member = getMember(playListDTO.getMemberId());
            assert member != null;

            PlayList playList = createPlayList(playListDTO,member);
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


    private Member getMember(Long memberId){

        
        QMember qMember = QMember.member;

        return jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(memberId))
                .fetchOne();
    }


    private PlayList createPlayList(PlayListDTO playListDTO, Member member) {

        // 플레이리스트 객체 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return PlayList.builder()
                .member(member)
                .playListNm(playListDTO.getPlayListNm())
                .isPlayListPrivacy(playListDTO.getIsPlayListPrivacy())
                .playListLikeCnt(0L)
                .isAlbum(playListDTO.isAlbum())
                .albumDate(playListDTO.isAlbum() ? LocalDateTime.now().format(formatter) : null)
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
