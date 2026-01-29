package com.dotwavesoftware.importscheduler.dto;

import java.time.LocalDateTime;

public record ImportProgressDTO(
    int importId,
    String status,
    int currentRecord,
    int totalRecords,
    String progressMessage,
    LocalDateTime completionDatetime
) {
    public int getProgressPercent() {
        if (totalRecords == 0) return 0;
        return (int) ((currentRecord * 100.0) / totalRecords);
    }
}
