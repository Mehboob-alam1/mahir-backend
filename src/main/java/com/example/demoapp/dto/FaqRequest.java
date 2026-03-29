package com.example.demoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqRequest {

    @NotBlank
    @Size(max = 500)
    private String question;

    @NotBlank
    @Size(max = 4000)
    private String answer;

    private Integer sortOrder;

    private Boolean active;
}
