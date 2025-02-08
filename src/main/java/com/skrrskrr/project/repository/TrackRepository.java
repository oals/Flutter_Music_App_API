package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track,Long>, QuerydslPredicateExecutor<Track> {




}
