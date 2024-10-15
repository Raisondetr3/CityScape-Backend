package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.CityAudit;

@Repository
public interface CityAuditRepository extends JpaRepository<CityAudit, Long> {
}
