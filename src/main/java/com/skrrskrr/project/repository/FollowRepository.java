package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow,Long>, QuerydslPredicateExecutor<Long> {
}
