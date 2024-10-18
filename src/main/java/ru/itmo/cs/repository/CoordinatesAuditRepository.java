package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.CoordinatesAudit;

import java.util.List;

@Repository
public interface CoordinatesAuditRepository extends JpaRepository<CoordinatesAudit, Long> {
}
