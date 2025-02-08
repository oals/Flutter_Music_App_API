package com.skrrskrr.project.repository;


import com.skrrskrr.project.dto.MemberDTO;
import com.skrrskrr.project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,String>, QuerydslPredicateExecutor<Member> {

}