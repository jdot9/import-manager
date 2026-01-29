package com.dotwavesoftware.importscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDTO {
    private String name;
    private String description;
    private String type;
    private String userUuid;
    private String five9Username;
    private String five9Password;
    private String hubspotAccessToken;
}
