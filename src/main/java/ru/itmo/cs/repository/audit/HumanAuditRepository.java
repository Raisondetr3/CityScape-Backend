package ru.itmo.cs.repository.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.audit.HumanAudit;

@Repository
public interface HumanAuditRepository extends JpaRepository<HumanAudit, Long> {
    void deleteAllByHumanId(Long humanId);
}
