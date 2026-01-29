package com.dotwavesoftware.importscheduler.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="import_schedules")
public class ImportScheduleEntity extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name="import_id")
    private ImportEntity importEntity;

    @Column(name="start_datetime")
    private LocalDateTime startDatetime;

    @Column(name="completion_datetime")
    private LocalDateTime completionDatetime;

    @Column(name="recurring")
    private boolean recurring;

    @Column(name="sunday")
    private boolean sunday;

    @Column(name="monday")
    private boolean monday;

    @Column(name="tuesday")
    private boolean tuesday;

    @Column(name="wednesday")
    private boolean wednesday;

    @Column(name="thursday")
    private boolean thursday;

    @Column(name="friday")
    private boolean friday;

    @Column(name="saturday")
    private boolean saturday;

    @Column(name="yearly")
    private boolean yearly;

    @Column(name="day")
    private int day;

    @Column(name="month")
    private int month;
}
