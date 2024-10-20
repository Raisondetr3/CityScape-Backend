package ru.itmo.cs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findById(Long id);
    Optional<City> findFirstByGovernment(Government government);
    @Query("SELECT SUM(c.metersAboveSeaLevel) FROM City c")
    Long sumMetersAboveSeaLevel();
    List<City> findByClimateGreaterThanEqual(Climate climate);
    City findTopByOrderByAreaDesc();
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
