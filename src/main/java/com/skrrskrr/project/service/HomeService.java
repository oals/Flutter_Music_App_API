package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.HomeRequestDto;
import com.skrrskrr.project.dto.HomeResponseDto;

public interface HomeService {

    HomeResponseDto firstLoad(HomeRequestDto homeRequestDto);

}
