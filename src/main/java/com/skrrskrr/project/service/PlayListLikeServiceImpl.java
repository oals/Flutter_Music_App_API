package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.PlayListLikeSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.PlayListSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.PlayListLikeUpdateQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.PlayListUpdateQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
@SuppressWarnings("unchecked")
public class PlayListLikeServiceImpl implements PlayListLikeService{


    @PersistenceContext
    EntityManager entitiyManager;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<String,Object> setPlayListLike(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            /// 해당 플리에 좋아요 기록이 있는지 조회
            PlayListLike playListLike = selectPlayListLikeEntity(playListRequestDto);

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

    @Override
    public PlayListLike selectPlayListLikeEntity(PlayListRequestDto playListRequestDto){

        PlayListLikeSelectQueryBuilder playListLikeSelectQueryBuilder = new PlayListLikeSelectQueryBuilder(jpaQueryFactory);

        return (PlayListLike) playListLikeSelectQueryBuilder
                .selectFrom(QPlayListLike.playListLike)
                .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                .findPlayListByPlayListId(playListRequestDto.getPlayListId())
                .fetchOne(PlayListLike.class);

    }

    @Override
    public Map<String, Object> getLikePlayList(PlayListRequestDto playListRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        PlayListLikeSelectQueryBuilder playListLikeSelectQueryBuilder = new PlayListLikeSelectQueryBuilder(jpaQueryFactory);

        try {


            List<PlayListDto> likePlayListDtoList = (List<PlayListDto>) playListLikeSelectQueryBuilder
                    .selectFrom(QPlayListLike.playListLike)
                    .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsPlayListLikeStatusTrue()
                    .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .orderByPlayListLikeDateDesc()
                    .offset(playListRequestDto.getOffset())
                    .limit(playListRequestDto.getLimit())
                    .fetchLikePlayListDto(PlayListDto.class);


            Long totalCount = playListLikeSelectQueryBuilder
                    .resetQuery()
                    .from(QPlayListLike.playListLike)
                    .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsPlayListLikeStatusTrue()
                    .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .fetchCount();


            hashMap.put("playList",likePlayListDtoList);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    private void insertPlayListLike(PlayListRequestDto playListRequestDto) {

        MemberPlayList memberPlayList = getMemberPlayList(playListRequestDto);

        Member member = Member.builder()
                .memberId(playListRequestDto.getLoginMemberId())
                .build();

        insertPlayListLikeStatus(member,memberPlayList);

        updatePlayListLikeCnt(memberPlayList.getPlayList(), false);

    }

    private void insertPlayListLikeStatus(Member member,MemberPlayList memberPlayList) {

        PlayListLike insertPlayListLike = new PlayListLike();
        insertPlayListLike.setMemberPlayList(memberPlayList);
        insertPlayListLike.setMember(member);
        insertPlayListLike.setPlayListLikeStatus(true);
        insertPlayListLike.setPlayListLikeDate(LocalDateTime.now());

        entitiyManager.persist(insertPlayListLike);
    }



    private void updatePlayListLike(PlayListRequestDto playListRequestDto, PlayListLike playListLike) {

        Boolean playListLikeStatus = playListLike.getPlayListLikeStatus();

        MemberPlayList memberPlayList = getMemberPlayList(playListRequestDto);

        updatePlayListLikeStatus(memberPlayList,playListLikeStatus,playListRequestDto.getLoginMemberId());

        updatePlayListLikeCnt(memberPlayList.getPlayList(),playListLikeStatus);

    }

    private void updatePlayListLikeStatus(MemberPlayList memberPlayList, Boolean playListLikeStatus,Long memberId){

        PlayListLikeUpdateQueryBuilder playListLikeUpdateQueryBuilder = new PlayListLikeUpdateQueryBuilder(entitiyManager);

        playListLikeUpdateQueryBuilder.setEntity(QPlayListLike.playListLike)
                .set(QPlayListLike.playListLike.playListLikeStatus, !playListLikeStatus)
                .set(QPlayListLike.playListLike.playListLikeDate, LocalDateTime.now())
                .findPlayListLikeByMemberPlayListId(memberPlayList.getMemberPlayListId())
                .findPlayListLikeByMemberId(memberId)
                .execute();
    }


    private void updatePlayListLikeCnt(PlayList playList, Boolean playListLikeStatus) {


        PlayListUpdateQueryBuilder playListUpdateQueryBuilder = new PlayListUpdateQueryBuilder(entitiyManager);

        playListUpdateQueryBuilder.setEntity(QPlayList.playList)
                .set(QPlayList.playList.playListLikeCnt, playListLikeStatus
                        ? playList.getPlayListLikeCnt() - 1
                        : playList.getPlayListLikeCnt() + 1)
                .findPlayListByPlayListId(playList.getPlayListId())
                .execute();

    }

    private MemberPlayList getMemberPlayList(PlayListRequestDto playListRequestDto) {

        PlayListSelectQueryBuilder playListSelectQueryBuilder = new PlayListSelectQueryBuilder(jpaQueryFactory);

        return (MemberPlayList) playListSelectQueryBuilder.selectFrom(QMemberPlayList.memberPlayList)
                .findPlayListsByPlayListId(playListRequestDto.getPlayListId())
                .fetchOne(PlayList.class);

    }

}
