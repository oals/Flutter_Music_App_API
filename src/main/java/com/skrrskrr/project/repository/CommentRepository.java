package com.skrrskrr.project.repository;

import com.skrrskrr.project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long>, QuerydslPredicateExecutor<Long> {
}
