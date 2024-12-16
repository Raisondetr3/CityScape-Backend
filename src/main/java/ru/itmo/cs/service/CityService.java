package ru.itmo.cs.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.dto.city.CityFilterCriteria;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CoordinatesService coordinatesService;
    private final HumanService humanService;
    private final UserService userService;
    private final AuditService auditService;
    private final EntityMapper entityMapper;
    private final FilterProcessor<CityDTO, CityFilterCriteria> cityFilterProcessor;
    private final PaginationHandler paginationHandler;
    private final CalculateDistanceService calculateDistanceService;

    @Transactional(readOnly = true)
    public Page<CityDTO> getAllCities(String name, Climate climate, Government government,
                                      StandardOfLiving standardOfLiving, String governorName,
                                      int page, int size, String sortBy, String sortDir) {
        CityFilterCriteria criteria = new CityFilterCriteria();
        criteria.setName(name);
        criteria.setClimate(climate);
        criteria.setGovernment(government);
        criteria.setStandardOfLiving(standardOfLiving);
        criteria.setGovernorName(governorName);

        Pageable pageable = paginationHandler.createPageable(page, size, sortBy, sortDir);
        return cityFilterProcessor.filter(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public CityDTO getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City не найден"));
        return entityMapper.toCityDTO(city);
    }

    @Transactional
    public CityDTO createCity(CityDTO cityDTO) {
        try {
            validateCityUniqueness(cityDTO);

            Coordinates savedCoordinates = coordinatesService
                    .createOrUpdateCoordinatesForCity(cityDTO.getCoordinates());
            Human savedHuman = humanService
                    .createOrUpdateHumanForCity(cityDTO.getGovernor());

            City city = entityMapper.toCityEntity(cityDTO, savedCoordinates, savedHuman);

            city.setCreatedBy(userService.getCurrentUser());
            city.setCreationDate(LocalDate.now());

            City savedCity = cityRepository.saveAndFlush(city);
            auditService.auditCity(savedCity, AuditOperation.CREATE);

            return entityMapper.toCityDTO(savedCity);
        } catch  (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public CityDTO updateCity(Long id, CityDTO cityDTO) {
        try {
            City existingCity = cityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("City не найден"));

            validateCityUniqueness(cityDTO);

            if (!userService.canModifyCity(existingCity)) {
                throw new SecurityException("У вас нет разрешения на изменение этого City");
            }

            Coordinates savedCoordinates = coordinatesService
                    .createOrUpdateCoordinatesForCity(cityDTO.getCoordinates());
            Human savedHuman = humanService
                    .createOrUpdateHumanForCity(cityDTO.getGovernor());

            City updatedCity = entityMapper.toCityEntity(cityDTO, savedCoordinates, savedHuman);

            updatedCity.setId(existingCity.getId()); // id – const
            updatedCity.setCreatedBy(existingCity.getCreatedBy()); // creator – const
            updatedCity.setCreationDate(existingCity.getCreationDate()); // // creation date – const

            City savedCity = cityRepository.save(updatedCity);
            auditService.auditCity(savedCity, AuditOperation.UPDATE);

            return entityMapper.toCityDTO(savedCity);
        } catch  (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City не найден"));

        if (!userService.canModifyCity(city)) {
            throw new SecurityException("У вас нет разрешения на удаление этого City");
        }

        auditService.deleteCityAuditEntries(city.getId());

        cityRepository.delete(city);
    }

    @Transactional
    public void deleteCityByGovernment(Government government) {
        City city = cityRepository.findFirstByGovernment(government)
                .orElseThrow(() ->
                        new EntityNotFoundException("City with government " + government + " not found"));

        if (!userService.canModifyCity(city)) {
            throw new SecurityException("You don't have permission to delete this city");
        }

        auditService.deleteCityAuditEntries(city.getId());

        cityRepository.delete(city);
    }

    @Transactional(readOnly = true)
    public Long calculateTotalMetersAboveSeaLevel() {
        return cityRepository.sumMetersAboveSeaLevel();
    }

    @Transactional(readOnly = true)
    public Long countCitiesByClimate(Climate climate) {
        return (long) cityRepository.findByClimateGreaterThan(climate).size();
    }

    @Transactional(readOnly = true)
    public double calculateRouteToCityWithLargestArea() {
        City cityWithLargestArea = cityRepository.findTopByOrderByAreaDesc();
        if (cityWithLargestArea != null) {
            return calculateDistanceService.calculate(0,
                                                      0,
                                                      0,
                                                      cityWithLargestArea.getCoordinates().getX(),
                                                      cityWithLargestArea.getCoordinates().getY(),
                                                      0);
        } else {
            throw new EntityNotFoundException("No cities found");
        }
    }

    @Transactional(readOnly = true)
    public double calculateRouteToCityWithLargestAreaFromUser(double userX, double userY, double userZ) {
        City cityWithLargestArea = cityRepository.findTopByOrderByAreaDesc();
        if (cityWithLargestArea != null) {
            return calculateDistanceService.calculate(userX,
                                                      userY,
                                                      userZ,
                                                      cityWithLargestArea.getCoordinates().getX(),
                                                      cityWithLargestArea.getCoordinates().getY(),
                                                      0);
        } else {
            throw new EntityNotFoundException("No cities found");
        }
    }

//    private void validateCityUniqueness(CityDTO cityDTO) {
//        boolean isNameDuplicate = cityRepository.findByFilters(cityDTO.getName(), null, Pageable.unpaged())
//                .stream()
//                .anyMatch(city -> city.getCoordinates().getId().equals(cityDTO.getCoordinates().getId()));
//
//        if (isNameDuplicate) {
//            throw new IllegalArgumentException("Город с таким именем и координатами уже существует");
//        }
//
//        boolean isNameGovernorDuplicate = cityRepository.findByFilters(cityDTO.getName(),
//                        cityDTO.getGovernor().getName(),
//                        Pageable.unpaged())
//                .stream()
//                .anyMatch(city -> city.getGovernor().getId().equals(cityDTO.getGovernor().getId()));
//
//        if (isNameGovernorDuplicate) {
//            throw new IllegalArgumentException("Город с таким именем и губернатором уже существует");
//        }
//    }

    private void validateCityUniqueness(CityDTO cityDTO) {
        List<City> citiesByNameAndCoordinates = cityRepository.findByNameAndCoordinatesForUpdate(
                cityDTO.getName(),
                cityDTO.getCoordinates().getX(),
                cityDTO.getCoordinates().getY()
        );

        if (!citiesByNameAndCoordinates.isEmpty()) {
            throw new IllegalArgumentException("Город с таким именем и координатами уже существует");
        }

        List<City> citiesByNameAndGovernor = cityRepository.findByNameAndGovernorForUpdate(
                cityDTO.getName(),
                cityDTO.getGovernor().getName(),
                cityDTO.getGovernor().getAge(),
                cityDTO.getGovernor().getHeight(),
                cityDTO.getGovernor().getBirthday()
        );

        if (!citiesByNameAndGovernor.isEmpty()) {
            throw new IllegalArgumentException("Город с таким именем и губернатором уже существует");
        }
    }
}