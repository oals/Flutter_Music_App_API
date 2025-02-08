package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History,Long> , QuerydslPredicateExecutor<Long> {
}
