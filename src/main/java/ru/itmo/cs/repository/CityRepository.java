package ru.itmo.cs.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;

import java.time.ZonedDateTime;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM City c WHERE c.name = :name AND c.coordinates.x = :x AND c.coordinates.y = :y")
    List<City> findByNameAndCoordinatesForUpdate(
            @Param("name") String name,
            @Param("x") Long x,
            @Param("y") Double y
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM City c " +
            "WHERE c.name = :name AND c.governor.name = :governorName " +
            "AND c.governor.age = :age AND c.governor.height = :height " +
            "AND c.governor.birthday = :birthday")
    List<City> findByNameAndGovernorForUpdate(
            @Param("name") String name,
            @Param("governorName") String governorName,
            @Param("age") int age,
            @Param("height") int height,
            @Param("birthday") ZonedDateTime birthday
    );

    City findTopByOrderByAreaDesc();
    @Query("SELECT c FROM City c " +
            "LEFT JOIN c.governor g " +
            "WHERE (:name IS NULL OR c.name LIKE %:name%) " +
            "AND (:governorName IS NULL OR g.name LIKE %:governorName%)")
    Page<City> findByFilters(@Param("name") String name,
                             @Param("governorName") String governorName,
                             Pageable pageable);
}
