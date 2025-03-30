package com.skrrskrr.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseQueryDto {

    private StringPath updateStringEntityPath;
    private BooleanPath updateBooleanEntityPath;

    @JsonIgnore
    private Map<Path, Path> joinTarget = new HashMap<>();

    private EntityPathBase<?> defaultJoinTarget;

    private Predicate joinCondition;

    private EntityPathBase<?> entityPathBase;

    private Object updateValue;

    private OrderSpecifier<?> orderSpecifier;

    private Expression<?> projections;

    private Expression<?> groupExpression;

    private BooleanExpression condition;

}
