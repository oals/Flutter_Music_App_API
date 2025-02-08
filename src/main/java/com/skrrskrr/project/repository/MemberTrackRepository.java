package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.MemberTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTrackRepository extends JpaRepository<MemberTrack,Long>, QuerydslPredicateExecutor<Long> {
}
