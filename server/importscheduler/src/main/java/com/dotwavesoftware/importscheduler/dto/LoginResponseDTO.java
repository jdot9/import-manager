package com.dotwavesoftware.importscheduler.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String token;
}

