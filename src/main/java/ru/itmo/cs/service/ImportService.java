package ru.itmo.cs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.ImportOperationDTO;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.entity.ImportOperation;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.ImportStatus;
import ru.itmo.cs.exception.JsonParsingException;
import ru.itmo.cs.repository.ImportOperationRepository;
import ru.itmo.cs.util.EntityMapper;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportOperationRepository importOperationRepository;
    private final CityService cityService;
    private final EntityMapper entityMapper;
    private final UserService userService;

    @Transactional
    public ImportOperationDTO importCities(String jsonData) {
        User currentUser = userService.getCurrentUser();
        ImportOperation importOperation = new ImportOperation();
        importOperation.setStatus(ImportStatus.IN_PROGRESS);
        importOperation.setTimestamp(LocalDateTime.now());
        importOperation.setUser(currentUser);
        log.info("Saving import operation with status: IN_PROGRESS for user: {}", currentUser.getUsername());
        importOperationRepository.save(importOperation);

        try {
            List<CityDTO> cities = parseJsonToCityDTOList(jsonData);
            log.info("Parsed {} cities from JSON data.", cities.size());

            for (CityDTO cityDTO : cities) {
                log.info("Creating city: {}", cityDTO.getName());
                cityService.createCity(cityDTO);
            }

            log.info("Import operation completed successfully, {} cities added.", cities.size());
            importOperation.setStatus(ImportStatus.SUCCESS);
            importOperation.setObjectsAdded(cities.size());
        } catch (RuntimeException e) {
            importOperation.setStatus(ImportStatus.FAILURE);
            importOperation.setObjectsAdded(0);
            log.error("Error during import operation: {}", e.getMessage(), e);
            throw new JsonParsingException("Ошибка парсинга JSON: " + e.getMessage(), e);
        } finally {
            log.info("Saving final import operation with status: {}", importOperation.getStatus());
            importOperationRepository.save(importOperation);
        }

        return entityMapper.toImportOperationDTO(importOperation);
    }

    @Transactional(readOnly = true)
    public List<ImportOperationDTO> getImportHistory() {
        log.info("Fetching import operation history");
        List<ImportOperation> operations = importOperationRepository.findByStatus(ImportStatus.SUCCESS);
        return operations.stream()
                .map(entityMapper::toImportOperationDTO)
                .toList();
    }

    private List<CityDTO> parseJsonToCityDTOList(String jsonData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<CityDTO> cities = objectMapper.readValue(jsonData, new TypeReference<List<CityDTO>>() {});
            log.debug("Parsed JSON data into {} CityDTO objects.", cities.size());
            return cities;
        } catch (Exception e) {
            log.error("Error parsing JSON data: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка парсинга JSON: " + e.getMessage(), e);
        }
    }
}