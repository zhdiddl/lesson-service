package com.learnhive.lessonservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LessonDto {

    @NotBlank(message = "레슨명은 필수입니다.")
    private String title;

    @NotBlank(message = "레슨 가격은 필수입니다.")
    private Integer price;

    private String description;

}
