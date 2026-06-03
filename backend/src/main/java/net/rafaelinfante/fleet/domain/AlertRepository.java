package net.rafaelinfante.fleet.domain;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findFirstByDeviceIdAndRuleIdAndStatusIn(
            String deviceId, Long ruleId, Collection<AlertStatus> statuses);

    Optional<Alert> findFirstByDeviceIdAndTypeAndStatusIn(
            String deviceId, AlertType type, Collection<AlertStatus> statuses);

    long countByStatus(AlertStatus status);

    @Query("""
            select a from Alert a
            where (:status is null or a.status = :status)
              and (:deviceId is null or a.deviceId = :deviceId)
            """)
    Page<Alert> search(@Param("status") AlertStatus status,
                       @Param("deviceId") String deviceId,
                       Pageable pageable);
}
