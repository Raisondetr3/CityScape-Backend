package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.util.EntityMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;

    public List<CityDTO> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(entityMapper::toCityDTO)
                .collect(Collectors.toList());
    }

    public CityDTO getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        return entityMapper.toCityDTO(city);
    }

    public CityDTO createCity(CityDTO cityDTO) {
        City city = entityMapper.toCityEntity(cityDTO);
        city.setCreatedBy(userService.getCurrentUser());
        city.setCreationDate(LocalDate.now());
        City savedCity = cityRepository.save(city);
        return entityMapper.toCityDTO(savedCity);
    }

    public CityDTO updateCity(Long id, CityDTO cityDTO) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));

        if (!userService.canModifyCity(existingCity)) {
            throw new SecurityException("You don't have permission to modify this city");
        }

        City updatedCity = entityMapper.toCityEntity(cityDTO);
        updatedCity.setId(existingCity.getId()); // id – const
        updatedCity.setCreatedBy(existingCity.getCreatedBy()); // Creator – const
        updatedCity.setCreationDate(existingCity.getCreationDate()); // Creation date – const

        City savedCity = cityRepository.save(updatedCity);
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

