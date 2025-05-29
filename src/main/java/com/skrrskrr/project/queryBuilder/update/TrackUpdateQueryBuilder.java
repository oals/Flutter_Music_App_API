package com.skrrskrr.project.queryBuilder.update;

import com.skrrskrr.project.entity.QTrack;
import jakarta.persistence.EntityManager;

public class TrackUpdateQueryBuilder extends ComnUpdateQueryBuilder<TrackUpdateQueryBuilder>{

    QTrack qTrack = QTrack.track;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public TrackUpdateQueryBuilder( EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    /** -------------------------- WHERE 조건 추가 ------------------------------ */

    public TrackUpdateQueryBuilder findTrackByTrackId(Long trackId) {
        if (this.updateClause == null) {
            throw new IllegalStateException("Entity must be set before adding conditions.");
        }
        this.updateClause.where(qTrack.trackId.eq(trackId));
        return this;
    }

}
