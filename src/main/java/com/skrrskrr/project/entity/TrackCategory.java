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
public class TrackCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackCategoryId;

    @ManyToOne
    @JoinColumn(name = "track_id")
    private Track track;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
