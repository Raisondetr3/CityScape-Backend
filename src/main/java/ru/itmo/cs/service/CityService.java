package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.dto.CityFilterCriteria;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.time.LocalDate;

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

    @Transactional(readOnly = true)
    public Page<CityDTO> getAllCities(String name, Climate climate, Government government, StandardOfLiving standardOfLiving, int page, int size, String sortBy, String sortDir) {
        CityFilterCriteria criteria = new CityFilterCriteria();
        criteria.setName(name);
        criteria.setClimate(climate);
        criteria.setGovernment(government);
        criteria.setStandardOfLiving(standardOfLiving);

        Pageable pageable = paginationHandler.createPageable(page, size, sortBy, sortDir);
        return cityFilterProcessor.filter(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public CityDTO getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        return entityMapper.toCityDTO(city);
    }

    @Transactional
    public CityDTO createCity(CityDTO cityDTO) {
        Coordinates savedCoordinates = coordinatesService
                .createOrUpdateCoordinatesForCity(cityDTO.getCoordinates());
        Human savedHuman = humanService
                .createOrUpdateHumanForCity(cityDTO.getGovernor());

        City city = entityMapper.toCityEntity(cityDTO, savedCoordinates, savedHuman);

        city.setCreatedBy(userService.getCurrentUser());
        city.setCreationDate(LocalDate.now());

        City savedCity = cityRepository.save(city);
        auditService.auditCity(savedCity, AuditOperation.CREATE);

        return entityMapper.toCityDTO(savedCity);
    }

    @Transactional
    public CityDTO updateCity(Long id, CityDTO cityDTO) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));

        if (!userService.canModifyCity(existingCity)) {
            throw new SecurityException("You don't have permission to modify this city");
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
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));

        if (!userService.canModifyCity(city)) {
            throw new SecurityException("You don't have permission to delete this city");
        }

        cityRepository.delete(city);
    }
}


