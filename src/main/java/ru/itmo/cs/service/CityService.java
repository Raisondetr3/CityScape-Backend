package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.entity.AuditOperation;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.repository.CoordinatesRepository;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.util.EntityMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final HumanRepository humanRepository;

    private final UserService userService;
    private final AuditService auditService;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<CityDTO> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(entityMapper::toCityDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CityDTO getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        return entityMapper.toCityDTO(city);
    }

    @Transactional
    public CityDTO createCity(CityDTO cityDTO) {
        Coordinates savedCoordinates = coordinatesRepository
                .save(entityMapper.toCoordinatesEntity(cityDTO.getCoordinates()));
        Human savedHuman = humanRepository
                .save(entityMapper.toHumanEntity(cityDTO.getGovernor()));

        City city = entityMapper.toCityEntity(cityDTO, savedCoordinates, savedHuman);

        city.setCreatedBy(userService.getCurrentUser());
        city.setCreationDate(LocalDate.now());

        // Deciding whether to create or update
        if (city.getCoordinates().getId() == null) {
            auditService.auditCoordinates(city.getCoordinates(), AuditOperation.CREATE);
        } else {
            auditService.auditCoordinates(city.getCoordinates(), AuditOperation.UPDATE);
        }

        if (city.getGovernor().getId() == null) {
            auditService.auditHuman(city.getGovernor(), AuditOperation.CREATE);
        } else {
            auditService.auditHuman(city.getGovernor(), AuditOperation.UPDATE);
        }

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

        Coordinates updatedCoordinates = coordinatesRepository
                .save(entityMapper.toCoordinatesEntity(cityDTO.getCoordinates()));
        Human updatedHuman = humanRepository
                .save(entityMapper.toHumanEntity(cityDTO.getGovernor()));

        City updatedCity = entityMapper.toCityEntity(cityDTO, updatedCoordinates, updatedHuman);

        updatedCity.setId(existingCity.getId()); // id – const
        updatedCity.setCreatedBy(existingCity.getCreatedBy()); // creator – const
        updatedCity.setCreationDate(existingCity.getCreationDate()); // // creation date – const

        // Deciding whether to create or update
        if (updatedCity.getCoordinates().getId() == null) {
            auditService.auditCoordinates(updatedCity.getCoordinates(), AuditOperation.CREATE);
        } else {
            auditService.auditCoordinates(updatedCity.getCoordinates(), AuditOperation.UPDATE);
        }

        if (updatedCity.getGovernor().getId() == null) {
            auditService.auditHuman(updatedCity.getGovernor(), AuditOperation.CREATE);
        } else {
            auditService.auditHuman(updatedCity.getGovernor(), AuditOperation.UPDATE);
        }

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

        auditService.auditCity(city, AuditOperation.DELETE);
        cityRepository.delete(city);
    }
}


