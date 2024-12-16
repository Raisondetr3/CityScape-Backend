package ru.itmo.cs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.cs.dto.ImportOperationDTO;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.entity.ImportOperation;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.ImportStatus;
import ru.itmo.cs.repository.ImportOperationRepository;
import ru.itmo.cs.util.EntityMapper;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportOperationRepository importOperationRepository;
    private final CityService cityService;
    private final EntityMapper entityMapper;
    private final UserService userService;
    private final MinIOService minIOService;

    private final DbTransactionResource dbResource;
    private final MinioTransactionResource minioResource;

    @Value("${minio.bucketName}")
    private String bucketName;

    public synchronized ImportOperationDTO importCities(MultipartFile file, String jsonData) throws IOException {
        User currentUser = userService.getCurrentUser();

        String fileName = "import-" + UUID.randomUUID() + ".json";
        minioResource.setFileData(fileName, file.getBytes());

        ImportOperation importOperation = new ImportOperation();
        importOperation.setStatus(ImportStatus.IN_PROGRESS);
        importOperation.setTimestamp(LocalDateTime.now());
        importOperation.setUser(currentUser);
        log.info("Saving import operation with status: IN_PROGRESS for user: {}", currentUser.getUsername());

        importOperationRepository.save(importOperation);

        int successfulImports = 0;
        int failedImports = 0;

        try {
            dbResource.prepare();
            minioResource.prepare();

            importOperationRepository.save(importOperation);

            List<CityDTO> cities = parseJsonToCityDTOList(jsonData);
            log.info("Parsed {} cities from JSON data.", cities.size());

            for (CityDTO cityDTO : cities) {
                try {
                    log.info("Creating city: {}", cityDTO.getName());
                    cityService.createCity(cityDTO);
                    successfulImports++;
                } catch (IllegalArgumentException e) {
                    log.warn("City '{}' не добавлен: {}", cityDTO.getName(), e.getMessage());
                    failedImports++;
                    throw e;
                }
            }

            if (failedImports == 0) {
                importOperation.setStatus(ImportStatus.SUCCESS);
            } else if (successfulImports == 0) {
                importOperation.setStatus(ImportStatus.FAILURE);
            } else {
                importOperation.setStatus(ImportStatus.PARTIAL_SUCCESS);
            }

            importOperation.setFileName(fileName);
            importOperation.setObjectsAdded(successfulImports);

            importOperationRepository.save(importOperation);

//            if (true) {
//                throw new RuntimeException("test test test");
//            }

            dbResource.commit();
            minioResource.commit();

            log.info("Import operation completed: {} cities added, {} failed.", successfulImports, failedImports);
            return entityMapper.toImportOperationDTO(importOperation);
        } catch (Exception e) {
            log.error("Error during import operation: {}", e.getMessage(), e);

            try {
                dbResource.rollback();
            } catch (Exception ex) {
                log.error("Error rolling back DB transaction: {}", ex.getMessage(), ex);
            }

            try {
                minioResource.rollback();
            } catch (Exception ex) {
                log.error("Error rolling back MinIO operation: {}", ex.getMessage(), ex);
            }

            throw new RuntimeException("Импорт не выполнен: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ImportOperationDTO> getImportHistory() {
        log.info("Fetching import operation history");
        List<ImportOperation> operations = importOperationRepository.findAll();
        return operations.stream()
                .map(operation -> {
                    ImportOperationDTO dto = entityMapper.toImportOperationDTO(operation);
                    if (operation.getFileName() != null) {
                        try {
                            String fileDownloadUrl = minIOService.getMinioClient().getPresignedObjectUrl(
                                    GetPresignedObjectUrlArgs.builder()
                                            .bucket(bucketName)
                                            .object(operation.getFileName())
                                            .method(Method.GET)
                                            .build()
                            );
                            dto.setFileDownloadUrl(getPresignedUrl(operation.getFileName()));
                        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                                 InvalidKeyException | InvalidResponseException | IOException |
                                 NoSuchAlgorithmException | XmlParserException | ServerException e) {
                            throw new RuntimeException("Ошибка при получении ссылки на файл в MinIO: " + e.getMessage(), e);
                        } catch (io.minio.errors.ErrorResponseException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    return dto;
                })
                .toList();
    }

    private String getPresignedUrl(String fileName) {
        return minioResource.getMinIOService().getPresignedUrl(fileName);
    }

    private List<CityDTO> parseJsonToCityDTOList(String jsonData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<CityDTO> cities = objectMapper.readValue(jsonData, new TypeReference<List<CityDTO>>() {});
            log.debug("Parsed JSON data into {} CityDTO objects.", cities.size());
            return cities;
        } catch (Exception e) {
            log.error("Error parsing JSON data: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка парсинга JSON: " + e.getMessage(), e);
        }
    }
}