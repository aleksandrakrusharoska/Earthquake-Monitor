package mk.codeit.earthquake.repository;

import mk.codeit.earthquake.domain.model.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {

    List<Earthquake> findByMagnitudeGreaterThanOrderByTimeDesc(Double magnitude);

    List<Earthquake> findByTimeAfterOrderByTimeDesc(Instant time);

    List<Earthquake> findAllByOrderByTimeDesc();

    boolean existsByUsgsId(String usgsId);

    @Modifying
    @Query("DELETE FROM Earthquake e")
    void deleteAllEarthquakes();
}
