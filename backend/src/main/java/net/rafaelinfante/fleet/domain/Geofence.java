package net.rafaelinfante.fleet.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A circular fenced area (centre + radius in metres) referenced by geofence alert rules. */
@Entity
@Table(name = "geofence")
@Getter
@Setter
@NoArgsConstructor
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lng", nullable = false)
    private double centerLng;

    @Column(name = "radius_m", nullable = false)
    private double radiusM;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
