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

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findById(Long id);

    Optional<City> findFirstByGovernment(Government government);

    @Query("SELECT SUM(c.metersAboveSeaLevel) FROM City c")
    Long sumMetersAboveSeaLevel();

    @Query("SELECT c FROM City c WHERE c.climate > :climate")
    List<City> findByClimateGreaterThan(@Param("climate") Climate climate);

    @Query("SELECT c FROM City c WHERE c.name = :name AND c.coordinates.id = :coordinatesId")
    Optional<City> findByNameAndCoordinates(@Param("name") String name, @Param("coordinatesId") Long coordinatesId);

    @Query("SELECT c FROM City c WHERE c.name = :name AND c.governor.id = :governorId")
    Optional<City> findByNameAndGovernor(@Param("name") String name, @Param("governorId") Long governorId);

    City findTopByOrderByAreaDesc();
    @Query("SELECT c FROM City c " +
            "LEFT JOIN c.governor g " +
            "WHERE (:name IS NULL OR c.name LIKE %:name%) " +
            "AND (:governorName IS NULL OR g.name LIKE %:governorName%)")
    Page<City> findByFilters(@Param("name") String name,
                             @Param("governorName") String governorName,
                             Pageable pageable);
}
