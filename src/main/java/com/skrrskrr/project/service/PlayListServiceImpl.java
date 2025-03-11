package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.PlayListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    EntityManager em;

    final private PlayListRepository playListRepository;

    @Override
    public Map<String,Object> getPlayList(Long memberId, Long trackId , Long listIndex,boolean isAlbum) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QPlayList qPlayList = QPlayList.playList;

        Map<String,Object> hashMap = new HashMap<>();


        try {
            List<PlayList> queryResult = jpaQueryFactory.selectFrom(qPlayList)
                    .where(qPlayList.member.memberId.eq(memberId)
                            .and(qPlayList.isAlbum.eq(isAlbum)))
                    .orderBy(qPlayList.playListId.desc())
                    .offset(listIndex)
                    .limit(20)
                    .fetch();


            List<PlayListDTO> list = new ArrayList<>();


            for (PlayList playList : queryResult) {
                PlayListDTO playListDTO = PlayListDTO.builder()
                        .memberId(playList.getMember().getMemberId())
                        .memberNickName(playList.getMember().getMemberNickName())
                        .playListLikeCnt(playList.getPlayListLikeCnt())
                        .playListId(playList.getPlayListId())
                        .playListNm(playList.getPlayListNm())
                        .isAlbum(playList.isAlbum())
                        .albumDate(playList.getAlbumDate())
                        .isPlayListPrivacy(playList.getIsPlayListPrivacy())
                        .trackCnt(0L)
                        .build();


                /// 트랙 검사 없이 가져올 때

                /// 해당 플리에 추가하려는 트랙이 존재하는지 검사
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

                list.add(playListDTO);
            }

            int totalCount = jpaQueryFactory
                    .selectFrom(qPlayList)
                    .where(qPlayList.member.memberId.eq(memberId)
                            .and(qPlayList.isAlbum.eq(isAlbum))
                    )
                    .fetch()
                    .size();


            hashMap.put("playList",list);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }





    @Override
    public Map<String,Object> getPlayListInfo(Long memberId, Long playListId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QPlayList qPlayList = QPlayList.playList;
        QPlayListLike qPlayListLike = QPlayListLike.playListLike;
        QTrackLike qTrackLike = QTrackLike.trackLike;
        Map<String,Object> hashMap = new HashMap<>();

        ///멤버 아이디도 전달해서 현재 플리의 주인 멤버아이디 == 전달받은 멤버아이디 비교 후 비공개 트랙 조회 x
        /// 수정 >  플리 주인이 아니라 트래


        try {
            PlayList queryResult = jpaQueryFactory.selectFrom(qPlayList)
                    .where(qPlayList.playListId.eq(playListId))
                    .fetchOne();


            boolean playListLikeStatus = Boolean.TRUE.equals(jpaQueryFactory.select(qPlayListLike.playListLikeStatus).from(qPlayListLike)
                    .where(qPlayListLike.memberPlayList.playList.playListId.eq(playListId)
                            .and(qPlayListLike.member.memberId.eq(memberId)))
                    .fetchOne());


            assert queryResult != null;

            int totalMinutes = 0;
            int totalSeconds = 0;

            // 플레이리스트의 주인이 아닌 경우 비공개 곡 제거
            List<Track> queryResultTemp = queryResult.getPlayListTrackList();

            PlayListDTO playListDTO = PlayListDTO.builder()
                    .playListId(queryResult.getPlayListId())
                    .playListNm(queryResult.getPlayListNm())
                    .playListLikeCnt(queryResult.getPlayListLikeCnt())  //나중에 수정 필요
                    .playListTrackList(new ArrayList<>())
                    .memberId(queryResult.getMember().getMemberId())
                    .memberNickName(queryResult.getMember().getMemberNickName())
                    .isPlayListPrivacy(queryResult.getIsPlayListPrivacy())
                    .isPlayListLike(playListLikeStatus)
                    .build();



            if (!queryResultTemp.isEmpty()){
                for (int i = queryResultTemp.size() - 1; i >= 0; i--) {
                    if (queryResultTemp.get(i).isTrackPrivacy()){
                        if(!Objects.equals(queryResult.getMember().getMemberId(), memberId)) {
                            continue;
                        }
                    }


                    TrackDTO trackDTO = TrackDTO.builder()
                            .trackId(queryResultTemp.get(i).getTrackId())
                            .trackNm(queryResultTemp.get(i).getTrackNm())
                            .trackPlayCnt(queryResultTemp.get(i).getTrackPlayCnt())
                            .trackImagePath(queryResultTemp.get(i).getTrackImagePath())
                            .trackCategoryId(queryResultTemp.get(i).getTrackCategoryId())
                            .trackTime(queryResultTemp.get(i).getTrackTime())
                            .build();

                    String[] parts = trackDTO.getTrackTime().split(":");
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);

                    totalMinutes += minutes;
                    totalSeconds += seconds;

                    playListDTO.getPlayListTrackList().add(trackDTO);

                }
            }


            playListDTO.setTrackCnt((long) playListDTO.getPlayListTrackList().size());


            totalMinutes += totalSeconds / 60;
            totalSeconds = totalSeconds % 60;

            // 분을 시로 변환
            int totalHours = totalMinutes / 60;
            totalMinutes = totalMinutes % 60;

            // 총합 출력
            playListDTO.setTotalPlayTime(String.format("%d:%02d:%02d", totalHours, totalMinutes, totalSeconds));
            hashMap.put("playList", playListDTO);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }

    @Override
    public Map<String,Object> setPlayListTrack(PlayListDTO playListDTO) {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
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

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
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

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QPlayListLike qPlayListLike = QPlayListLike.playListLike;
        QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;
        Map<String,Object> hashMap = new HashMap<>();
        QMember qMember = QMember.member;

        try {

            /// 해당 플리에 좋아요 기록이 있는지 조회
            PlayListLike playListLike = jpaQueryFactory.selectFrom(qPlayListLike)
                    .where(qPlayListLike.memberPlayList.playList.playListId.eq(playListDTO.getPlayListId())
                            .and(qPlayListLike.member.memberId.eq(playListDTO.getMemberId())))
                    .fetchFirst();


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



    private void insertPlayListLike(PlayListDTO playListDTO) {


        MemberPlayList memberPlayList = getMemberPlayList(playListDTO.getPlayListId());

        Member member = Member.builder()
                .memberId(playListDTO.getMemberId())
                .build();


        PlayListLike insertPlayListLike = new PlayListLike();
        insertPlayListLike.setMemberPlayList(memberPlayList);
        insertPlayListLike.setMember(member);
        insertPlayListLike.setPlayListLikeStatus(true);

        em.persist(insertPlayListLike);


        PlayList playList = memberPlayList.getPlayList();
        playList.setPlayListLikeCnt(playList.getPlayListLikeCnt() + 1);  // 좋아요 수 증가

        // 변경된 PlayList 엔티티를 merge하여 업데이트
        em.merge(playList);  // 기존 엔티티 상태를 업데이트

    }


    private void updatePlayListLike(PlayListDTO playListDTO, PlayListLike playListLike) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QPlayListLike qPlayListLike = QPlayListLike.playListLike;

        boolean trackLikeStatus = playListLike.isPlayListLikeStatus();

        MemberPlayList memberPlayList = getMemberPlayList(playListDTO.getPlayListId());

        updatePlayListLikeStatus(trackLikeStatus,memberPlayList);


        updatePlayListLikeCnt(memberPlayList,trackLikeStatus);

    }


    private void updatePlayListLikeStatus(MemberPlayList memberPlayList, boolean trackLikeStatus){

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
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
        em.merge(playList);  // 기존 엔티티 상태를 업데이트
    }





    private MemberPlayList getMemberPlayList(Long playListId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
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

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
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
