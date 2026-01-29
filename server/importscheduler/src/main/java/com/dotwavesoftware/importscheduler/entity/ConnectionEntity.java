package com.dotwavesoftware.importscheduler.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="connections")
public class ConnectionEntity extends BaseEntity {

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="type")
    private String type;
    
    @Column(name="status")
    private String status;

    @Column(name="five9_username")
    private String five9Username;

    @Column(name="five9_password")
    private String five9Password;

    @Column(name="hubspot_access_token")
    private String hubspotAccessToken;

    @ManyToOne
    @JoinColumn(name="import_id")
    private ImportEntity importEntity;

    @ManyToOne
    @JoinColumn(name="user_uuid")
    private UserEntity user;

    @OneToMany(mappedBy="sendingConnection")
    private List<ConnectionImportMappingEntity> sendingMappings;

    @OneToMany(mappedBy="receivingConnection")
    private List<ConnectionImportMappingEntity> receivingMappings;
}
