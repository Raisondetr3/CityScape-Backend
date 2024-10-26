package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRole(UserRole role);
    List<User> findAllByPendingAdminApprovalTrue();
    Optional<User> findByUsername(String username);
}


