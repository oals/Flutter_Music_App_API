package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.util.List;

public class PlayListSelectQueryBuilder extends ComnSelectQueryBuilder<PlayListSelectQueryBuilder> {


    QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;

    public PlayListSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);  // 상위 클래스의 생성자 호출
    }

    public PlayListSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */


    public PlayListSelectQueryBuilder findPlayListsByMemberId (Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.query.where(qMemberPlayList.playList.member.memberId.eq(loginMemberId));
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListsById (Long playListId) {
        throwIfConditionNotMet(playListId != null);
        this.query.where(qMemberPlayList.playList.playListId.eq(playListId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsAlbum (Boolean isAlbum) {
        throwIfConditionNotMet(isAlbum != null);
        this.query.where(qMemberPlayList.playList.isAlbum.eq(isAlbum));
        return this;
    }

    public PlayListSelectQueryBuilder findIsInPlayListTrack (Long trackId) {
        throwIfConditionNotMet(trackId != null);
        this.query.where(qMemberPlayList.playList.playListTrackList.any().trackId.eq(trackId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListNotEmpty () {
        this.query.where(qMemberPlayList.playList.playListTrackList.isNotEmpty());
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListBySearchText (String searchText) {
        throwIfConditionNotMet(searchText != null);

        this.query.where(qMemberPlayList.playList.playListNm.contains(searchText));
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalse () {
        this.query.where(qMemberPlayList.playList.isPlayListPrivacy.isFalse());
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.findIsPlayListPrivacyFalse();
        this.query.where(qMemberPlayList.playList.isPlayListPrivacy.isFalse()
                .or(qMemberPlayList.playList.member.memberId.eq(loginMemberId)));
        return this;
    }



    /** --------------------------join -------------------------------------------*/


    /** --------------------------ordeBy ---------------------------------------- */

    public PlayListSelectQueryBuilder orderByPlayListIdDesc() {
        this.query.orderBy(qMemberPlayList.playList.playListId.desc());
        return this;
    }

    public PlayListSelectQueryBuilder orderByPlayListLikeCntDesc() {
        this.query.orderBy(qMemberPlayList.playList.playListLikeCnt.desc());
        return this;
    }


    /** -------------------------fetch ------------------------------------------- */

    public <T> T fetchPlayListEntity(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList
                )
        ).fetchFirst();
    }


    public <T> List<?> fetchPlayListPreviewDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList.playListId,
                        qMemberPlayList.playList.playListNm,
                        qMemberPlayList.playList.playListImagePath,
                        qMemberPlayList.playList.member.memberId,
                        qMemberPlayList.playList.member.memberNickName
                )
        ).fetch();
    }


    public <T> List<?> fetchPlayListsDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList.playListId,
                        qMemberPlayList.playList.playListNm,
                        qMemberPlayList.playList.playListLikeCnt,
                        qMemberPlayList.playList.isPlayListPrivacy,
                        qMemberPlayList.playList.isAlbum,
                        qMemberPlayList.playList.trackCnt,
                        qMemberPlayList.playList.playListImagePath,
                        qMemberPlayList.playList.albumDate,
                        qMemberPlayList.playList.member.memberId,
                        qMemberPlayList.playList.member.memberNickName
                )
        ).fetch();
    }


}
