package com.skrrskrr.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower;

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private Member following;




}
