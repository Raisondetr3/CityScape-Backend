package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.HumanAudit;

@Repository
public interface HumanAuditRepository extends JpaRepository<HumanAudit, Long> {
}