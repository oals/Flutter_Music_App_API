package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    private String historyText;

    private LocalDate historyDate;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

}
