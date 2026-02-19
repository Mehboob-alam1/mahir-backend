package com.example.demoapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Il nome non può essere vuoto")
    @Size(min = 1, max = 100)
    private String name;

    @NotBlank(message = "L'email non può essere vuota")
    @Email(message = "Formato email non valido")
    @Size(max = 255)
    private String email;
}
