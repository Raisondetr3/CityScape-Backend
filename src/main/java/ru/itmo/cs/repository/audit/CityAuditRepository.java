package ru.itmo.cs.repository.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.audit.CityAudit;

@Repository
public interface CityAuditRepository extends JpaRepository<CityAudit, Long> {
}
