package mk.codeit.earthquake.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "earthquakes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Earthquake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usgsId;

    @Column(nullable = false)
    private Double magnitude;

    @Column(length = 50)
    private String magType;

    @Column(nullable = false, length = 500)
    private String place;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false)
    private Instant time;

    private Double latitude;
    private Double longitude;
    private Double depth;

    @Column(nullable = false, updatable = false)
    private Instant fetchedAt;

    @PrePersist
    protected void onCreate() {
        this.fetchedAt = Instant.now();
    }
}
