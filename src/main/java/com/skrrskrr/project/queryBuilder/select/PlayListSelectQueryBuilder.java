package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.util.List;

public class PlayListSelectQueryBuilder extends ComnSelectQueryBuilder<PlayListSelectQueryBuilder> {


    QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;
    QPlayList qPlayList = QPlayList.playList;

    public PlayListSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);  // 상위 클래스의 생성자 호출
    }

    public PlayListSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */


    public PlayListSelectQueryBuilder findPlayListsByMemberId (Long loginMemberId) {
        this.query.where(qMemberPlayList.playList.member.memberId.eq(loginMemberId));
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListsById (Long playListId) {
        this.query.where(qMemberPlayList.playList.playListId.eq(playListId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsAlbum (Boolean isAlbum) {
        this.query.where(QMemberPlayList.memberPlayList.playList.isAlbum.eq(isAlbum));
        return this;
    }

    public PlayListSelectQueryBuilder findIsInPlayListTrack (Long trackId) {
        this.query.where(qMemberPlayList.playList.playListTrackList.any().trackId.eq(trackId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListNotEmpty () {
        this.query.where(qMemberPlayList.playList.playListTrackList.isNotEmpty());
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListBySearchText (String searchText) {
        if (searchText != null) {
            this.query.where(qMemberPlayList.playList.playListNm.contains(searchText));
        }
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalse () {
        this.query.where(qMemberPlayList.playList.isPlayListPrivacy.isFalse());
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {
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
        this.query.orderBy(QMemberPlayList.memberPlayList.playList.playListLikeCnt.desc());
        return this;
    }





    /** -------------------------fetch ------------------------------------------- */

    public Boolean fetchIsInPlayListTrack() {
        Boolean trackLikeStatus = this.query.select(QTrackLike.trackLike.trackLikeStatus)
                .fetchFirst();

        return trackLikeStatus != null && trackLikeStatus;
    }

    public <T> T fetchPlayListEntity(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberPlayList.memberPlayList.playList
                )
        ).fetchFirst();
    }


    public <T> List<?> fetchPlayListPreviewDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberPlayList.memberPlayList.playList.playListId,
                        QMemberPlayList.memberPlayList.playList.playListNm,
                        QMemberPlayList.memberPlayList.playList.playListImagePath,
                        QMemberPlayList.memberPlayList.playList.member.memberId,
                        QMemberPlayList.memberPlayList.playList.member.memberNickName
                )
        ).fetch();
    }


    public <T> List<?> fetchPlayListsDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberPlayList.memberPlayList.playList.playListId,
                        QMemberPlayList.memberPlayList.playList.playListNm,
                        QMemberPlayList.memberPlayList.playList.playListLikeCnt,
                        QMemberPlayList.memberPlayList.playList.isPlayListPrivacy,
                        QMemberPlayList.memberPlayList.playList.isAlbum,
                        QMemberPlayList.memberPlayList.playList.trackCnt,
                        QMemberPlayList.memberPlayList.playList.playListImagePath,
                        QMemberPlayList.memberPlayList.playList.albumDate,
                        QMemberPlayList.memberPlayList.playList.member.memberId,
                        QMemberPlayList.memberPlayList.playList.member.memberNickName
                )
        ).fetch();
    }


}
