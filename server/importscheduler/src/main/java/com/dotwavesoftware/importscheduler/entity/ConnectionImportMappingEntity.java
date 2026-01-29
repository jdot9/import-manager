package com.dotwavesoftware.importscheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="connection_import_mappings")
public class ConnectionImportMappingEntity {

    @EmbeddedId
    private ConnectionImportIdKey id;

    @Column(name="uuid", nullable = false, unique = true, updatable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID uuid;

    @ManyToOne
    @MapsId("sendingConnectionId")
    @JoinColumn(name="sending_connection_id")
    private ConnectionEntity sendingConnection;

    @ManyToOne
    @MapsId("receivingConnectionId")
    @JoinColumn(name="receiving_connection_id")
    private ConnectionEntity receivingConnection;

    @ManyToOne
    @MapsId("importId")
    @JoinColumn(name="import_id")
    private ImportEntity importEntity;

    @ManyToOne
    @JoinColumn(name="mapping_format_id")
    private MappingFormatEntity mappingFormat;

    @Column(name="five9_key")
    private Boolean five9Key = false;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="modified_at")
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) uuid = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
