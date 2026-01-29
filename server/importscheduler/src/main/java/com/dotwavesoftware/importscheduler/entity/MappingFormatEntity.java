package com.dotwavesoftware.importscheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="mapping_formats")
public class MappingFormatEntity extends BaseEntity{

    @OneToMany(mappedBy="mappingFormat")
    private List<ConnectionImportMappingEntity> connectionImportMappings;

    @Column(name="format")
    private String format;
}
