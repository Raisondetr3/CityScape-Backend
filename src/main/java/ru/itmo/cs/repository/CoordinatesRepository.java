package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Coordinates;

import java.util.Optional;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {
    Optional<Coordinates> findById(Long id);
}


