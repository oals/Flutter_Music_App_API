package com.skrrskrr.project.dto;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDto {

    Long historyId;

    String historyText;

    LocalDate historyDate;


}
