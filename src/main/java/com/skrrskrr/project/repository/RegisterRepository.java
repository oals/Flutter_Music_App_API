package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterRepository extends JpaRepository<Member,String>, QuerydslPredicateExecutor<Member> {
}
