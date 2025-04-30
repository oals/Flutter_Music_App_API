package com.skrrskrr.project.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.CategoryResponseDto;
import com.skrrskrr.project.entity.Category;
import com.skrrskrr.project.entity.QCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryServiceImpl implements CategoryService {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public CategoryResponseDto getCategoryList() {

        QCategory qCategory = QCategory.category;

        List<String> categoryNmList = jpaQueryFactory
                .select(qCategory.trackCategoryNm)
                .from(qCategory)
                .orderBy(qCategory.trackCategoryId.asc())
                .fetch();

        return CategoryResponseDto.builder()
                .categoryNmList(categoryNmList)
                .build();
    }
}
