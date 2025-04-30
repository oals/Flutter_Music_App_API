package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.CategoryResponseDto;
import com.skrrskrr.project.dto.CommentRequestDto;
import com.skrrskrr.project.dto.CommentResponseDto;
import com.skrrskrr.project.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(value = "/api/getCategoryList")
    public ResponseEntity<CategoryResponseDto> getCategoryList() {
        log.info("getCategoryList");
        CategoryResponseDto categoryResponseDto = categoryService.getCategoryList();
        return ResponseEntity.ok(categoryResponseDto);
    }



}
