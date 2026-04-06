package com.example.demoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    /** Must be exactly {@code DELETE} when using POST /users/me/delete. */
    private String confirm;
}
