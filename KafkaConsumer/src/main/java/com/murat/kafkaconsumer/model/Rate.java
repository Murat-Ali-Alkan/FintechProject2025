package com.murat.kafkaconsumer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="rates")
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_name")
    private String rateName;

    private double bid;

    private double ask;

    @Column(name="rate_update_time")
    private String timestamp;

    @Column(name="db_update_time")
    private Timestamp dbUpdateTime;

    @PrePersist
    protected void onCreate() {
        dbUpdateTime = new Timestamp(System.currentTimeMillis());
    }


}
