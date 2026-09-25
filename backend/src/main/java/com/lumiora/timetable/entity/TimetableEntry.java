package com.lumiora.timetable.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.lumiora.batch.entity.Batch;
import com.lumiora.entity.base.BaseEntity;
import com.lumiora.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "timetable_entries")
@Getter
@Setter
@NoArgsConstructor
public class TimetableEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 100)
    private String room;

    @Column(length = 200)
    private String topic;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;
}