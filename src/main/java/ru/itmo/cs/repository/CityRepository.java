package ru.itmo.cs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Climate;
import ru.itmo.cs.entity.Government;
import ru.itmo.cs.entity.StandardOfLiving;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findById(Long id);
    Page<City> findByNameContaining(String name, Pageable pageable);
    @Query("SELECT c FROM City c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:climate IS NULL OR c.climate = :climate) AND " +
            "(:government IS NULL OR c.government = :government) AND " +
            "(:standardOfLiving IS NULL OR c.standardOfLiving = :standardOfLiving)")
    Page<City> findByFilters(@Param("name") String name,
                             @Param("climate") Climate climate,
                             @Param("government") Government government,
                             @Param("standardOfLiving") StandardOfLiving standardOfLiving,
                             Pageable pageable);
}
