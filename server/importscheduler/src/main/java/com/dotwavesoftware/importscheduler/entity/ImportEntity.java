package com.dotwavesoftware.importscheduler.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="imports")
public class ImportEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="user_uuid", referencedColumnName="uuid")
    private UserEntity user;

    @OneToMany(mappedBy="importEntity")
    private List<ConnectionEntity> connections;

    @OneToMany(mappedBy="importEntity")
    private List<ImportScheduleEntity> importSchedules;

    @OneToMany(mappedBy="importEntity")
    private List<ConnectionImportMappingEntity> connectionImportMappings;

    @Column(name="name")
    private String name;

    @Column(name="status")
    private String status;

    @Column(name="email_notification")
    private boolean emailNotification;

    @Column(name="email")
    private String email;

    @Column(name="hubspot_list_id")
    private String hubspotListId;

    @Column(name="records_imported")
    private Integer recordsImported;

    @Column(name="total_records")
    private Integer totalRecords;

    @Column(name="progress")
    private Integer progress;
}
