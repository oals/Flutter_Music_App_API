package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.PlayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayListRepository extends JpaRepository<PlayList,Long>, QuerydslPredicateExecutor<Long> {
}
