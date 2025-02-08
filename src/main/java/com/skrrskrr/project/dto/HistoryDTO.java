package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDTO {

    Long historyId;

    String historyText;

    String historyDate;


}
