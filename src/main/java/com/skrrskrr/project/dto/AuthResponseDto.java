package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    private String jwtToken;

    private String refreshToken;

}
