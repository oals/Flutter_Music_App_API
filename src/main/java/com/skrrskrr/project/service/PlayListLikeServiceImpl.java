package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.PlayListResponseDto;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.PlayListLikeSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.PlayListSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.PlayListLikeUpdateQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.PlayListUpdateQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public void setPlayListLike(PlayListRequestDto playListRequestDto) {

        PlayListLike playListLike = selectPlayListLikeEntity(playListRequestDto);

        if (playListLike == null) {
            insertPlayListLike(playListRequestDto);
        } else {
            updatePlayListLike(playListRequestDto, playListLike);
        }
    }

    private PlayListLike selectPlayListLikeEntity(PlayListRequestDto playListRequestDto){

        PlayListLikeSelectQueryBuilder playListLikeSelectQueryBuilder = new PlayListLikeSelectQueryBuilder(jpaQueryFactory);

        return (PlayListLike) playListLikeSelectQueryBuilder
                .selectFrom(QPlayListLike.playListLike)
                .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                .findPlayListByPlayListId(playListRequestDto.getPlayListId())
                .fetchOne(PlayListLike.class);

    }

    @Override
    public PlayListResponseDto getLikePlayList(PlayListRequestDto playListRequestDto) {

        PlayListLikeSelectQueryBuilder playListLikeSelectQueryBuilder = new PlayListLikeSelectQueryBuilder(jpaQueryFactory);
        List<PlayListDto> likePlayListDtoList = new ArrayList<>();

        Long totalCount = playListLikeSelectQueryBuilder
                .resetQuery()
                .from(QPlayListLike.playListLike)
                .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                .findIsPlayListLikeStatusTrue()
                .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .fetchCount();

        if (totalCount != 0L) {
            likePlayListDtoList = (List<PlayListDto>) playListLikeSelectQueryBuilder
                    .selectFrom(QPlayListLike.playListLike)
                    .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                    .findIsPlayListLikeStatusTrue()
                    .findIsPlayListPrivacyFalseOrLoginMemberIdEqual(playListRequestDto.getLoginMemberId())
                    .findIsAlbum(playListRequestDto.getIsAlbum())
                    .orderByPlayListLikeDateDesc()
                    .offset(playListRequestDto.getOffset())
                    .limit(playListRequestDto.getLimit())
                    .fetchLikePlayListDto(PlayListDto.class);
        }

        return PlayListResponseDto.builder()
                .playLists(likePlayListDtoList)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public List<Long> getRecommendLikePlayListsMemberId(PlayListRequestDto playListRequestDto) {

        PlayListLikeSelectQueryBuilder playListLikeSelectQueryBuilder = new PlayListLikeSelectQueryBuilder(jpaQueryFactory);

        return playListLikeSelectQueryBuilder
                .selectFrom(QPlayListLike.playListLike)
                .findIsPlayListPrivacyFalse()
                .findPlayListsByNotMemberId(playListRequestDto.getLoginMemberId())
                .findPlayListLikeByMemberId(playListRequestDto.getLoginMemberId())
                .findIsAlbum(playListRequestDto.getIsAlbum())
                .orderByPlayListLikeDateDesc()
                .distinct()
                .limit(10L)
                .fetchPlayListByMemberIdList();
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
