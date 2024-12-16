package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.ImportOperation;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.ImportStatus;

import java.util.List;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    List<ImportOperation> findByStatus(ImportStatus status);
}
