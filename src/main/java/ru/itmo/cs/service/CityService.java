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
import ru.itmo.cs.util.EntityMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CoordinatesService coordinatesService;
    private final HumanService humanService;

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


