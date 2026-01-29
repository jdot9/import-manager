package com.dotwavesoftware.importscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HubSpotListDTO {
    private String listId;
    private String name;
    private String processingType;
    private String listSize;
    private String lastUpdated;
    private String objectTypeId;
}

