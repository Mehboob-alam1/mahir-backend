package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppContactResponse {

    /** Job poster's phone number for WhatsApp (e.g. 923001234567). */
    private String posterPhoneNumber;

    /** Remaining credits for the Mahir after this contact. */
    private int remainingCredits;
}
