package ru.itmo.cs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Human;

import java.util.Optional;

@Repository
public interface HumanRepository extends JpaRepository<Human, Long> {
    Optional<Human> findById(Long id);
    Page<Human> findByNameContaining(String name, Pageable pageable);
}


