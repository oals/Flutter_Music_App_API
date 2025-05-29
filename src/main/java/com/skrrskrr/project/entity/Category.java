package com.skrrskrr.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackCategoryId;

    private String trackCategoryNm;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    private List<TrackCategory> trackCategoryList; // TrackCategory와의 관계 추가

}
