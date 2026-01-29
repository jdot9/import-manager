package com.dotwavesoftware.importscheduler.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponseDTO {
    private Integer id;
    private String uuid;
    private String name;
    private String status;
    private Boolean emailNotification;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime startDatetime;
    private LocalDateTime completionDatetime;
    private Integer recordsImported;
    private Integer totalRecords;
    private Integer progress;
}
