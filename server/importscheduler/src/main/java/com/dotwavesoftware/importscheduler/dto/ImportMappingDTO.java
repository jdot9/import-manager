package com.dotwavesoftware.importscheduler.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImportMappingDTO {

    private String userUuid;
    private String importName;
    private Boolean emailNotifications;
    private String email;
    private List<MappingItem> mapping;
    private Schedule schedule;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class MappingItem {
        private Integer id;
        private Integer hubspotConnectionId;
        private String hubspotListId;
        private Integer five9ConnectionId;
        private String five9DialingList;
        private String hubspotProperty;
        private String five9Field;
        private Integer five9Key;
        private Integer formatId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Schedule {
        private String startDate;
        private String stopDate;
        private Boolean recurring;
        private Boolean daily;
        private Boolean monthly;
        private Boolean yearly;
        private Boolean indefinetely;
        private Boolean immediately;
        private Boolean sunday;
        private Boolean monday;
        private Boolean tuesday;
        private Boolean wednesday;
        private Boolean thursday;
        private Boolean friday;
        private Boolean saturday;
        private String day;
        private String month;
    }

}
