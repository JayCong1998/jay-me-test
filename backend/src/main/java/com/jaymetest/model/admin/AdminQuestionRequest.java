package com.jaymetest.model.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminQuestionRequest {

    @NotBlank
    @Pattern(regexp = "LYRICS|ALBUM")
    private String category;

    private String album;

    @NotBlank
    @Pattern(regexp = "EASY|MEDIUM|HARD")
    private String difficulty;

    @NotBlank
    private String questionText;

    @NotBlank
    private String optionA;

    @NotBlank
    private String optionB;

    @NotBlank
    private String optionC;

    @NotBlank
    private String optionD;

    @NotBlank
    @Pattern(regexp = "A|B|C|D")
    private String correctOption;

    @NotBlank
    private String explanation;
}
