package ru.itmo.cs.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.itmo.cs.dto.ImportOperationDTO;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.entity.ImportOperation;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.ImportStatus;
import ru.itmo.cs.repository.ImportOperationRepository;
import ru.itmo.cs.service.CityService;
import ru.itmo.cs.service.ImportService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportServiceTest {

    @InjectMocks
    private ImportService importService;

    @Mock
    private ImportOperationRepository importOperationRepository;

    @Mock
    private CityService cityService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private UserService userService;

    private User testUser;
    private ImportOperation importOperation;
    private ImportOperationDTO importOperationDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        importOperation = new ImportOperation();
        importOperation.setId(1L);
        importOperation.setStatus(ImportStatus.IN_PROGRESS);
        importOperation.setTimestamp(LocalDateTime.now());
        importOperation.setUser(testUser);

        importOperationDTO = new ImportOperationDTO();
        importOperationDTO.setId(1L);
        importOperationDTO.setStatus(ImportStatus.SUCCESS);
        importOperationDTO.setTimestamp(LocalDateTime.now());
        importOperationDTO.setObjectsAdded(2);

        when(userService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("Успешный импорт городов")
    void shouldImportCitiesSuccessfully() {
        // Arrange
        String jsonData = "[{\"name\": \"City1\"}, {\"name\": \"City2\"}]";

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(importOperationRepository.save(any(ImportOperation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cityService.createCity(any(CityDTO.class))).thenReturn(new CityDTO());
        when(entityMapper.toImportOperationDTO(argThat(operation ->
                operation.getStatus() == ImportStatus.SUCCESS &&
                        operation.getObjectsAdded() == 2 &&
                        operation.getUser().equals(testUser)
        ))).thenReturn(importOperationDTO);

        // Act
        ImportOperationDTO result = importService.importCities(jsonData);

        // Assert
        assertNotNull(result);
        assertEquals(ImportStatus.SUCCESS, result.getStatus());
        assertEquals(2, result.getObjectsAdded());
        verify(userService).getCurrentUser();
        verify(importOperationRepository, times(2)).save(any(ImportOperation.class));
        verify(cityService, times(2)).createCity(any(CityDTO.class));
        verify(entityMapper).toImportOperationDTO(any(ImportOperation.class));
    }


    @Test
    @DisplayName("Ошибка парсинга JSON")
    void shouldThrowExceptionForInvalidJson() {
        // Arrange
        String invalidJsonData = "invalid_json";

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(importOperationRepository.save(any(ImportOperation.class))).thenReturn(importOperation);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> importService.importCities(invalidJsonData)
        );

        assertTrue(exception.getMessage().contains("Ошибка парсинга JSON"));
        verify(importOperationRepository, times(2)).save(any(ImportOperation.class)); // Для IN_PROGRESS и FAILURE
        verify(cityService, never()).createCity(any(CityDTO.class));
    }

    @Test
    @DisplayName("Ошибка при создании города")
    void shouldRollbackTransactionOnCityCreationFailure() {
        // Arrange
        String jsonData = "[{\"name\": \"City1\"}]";

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(importOperationRepository.save(any(ImportOperation.class))).thenReturn(importOperation);
        when(cityService.createCity(any(CityDTO.class))).thenThrow(new RuntimeException("Ошибка при создании города"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> importService.importCities(jsonData)
        );

        assertTrue(exception.getMessage().contains("Ошибка при создании города"));
        verify(importOperationRepository, times(2)).save(any(ImportOperation.class)); // Для IN_PROGRESS и FAILURE
        verify(cityService).createCity(any(CityDTO.class));
    }

    @ParameterizedTest
    @DisplayName("Проверка корректного количества объектов в операции импорта")
    @MethodSource("provideJsonDataAndExpectedCount")
    void shouldCorrectlyCountImportedObjects(String jsonData, int expectedCount) {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(importOperationRepository.save(any(ImportOperation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cityService.createCity(any(CityDTO.class))).thenReturn(new CityDTO());
        when(entityMapper.toImportOperationDTO(any(ImportOperation.class)))
                .thenAnswer(invocation -> {
                    ImportOperation operation = invocation.getArgument(0);
                    ImportOperationDTO dto = new ImportOperationDTO();
                    dto.setId(operation.getId());
                    dto.setStatus(operation.getStatus());
                    dto.setTimestamp(operation.getTimestamp());
                    dto.setObjectsAdded(operation.getObjectsAdded());
                    dto.setUsername(operation.getUser().getUsername());
                    return dto;
                });

        // Act
        ImportOperationDTO result = importService.importCities(jsonData);

        // Assert
        assertNotNull(result, "ImportOperationDTO должен быть возвращен, но был null");
        assertEquals(expectedCount, result.getObjectsAdded(),
                "Количество добавленных объектов в ImportOperationDTO не соответствует ожидаемому.");
        verify(cityService, times(expectedCount)).createCity(any(CityDTO.class));
        verify(importOperationRepository, times(2)).save(any(ImportOperation.class)); // IN_PROGRESS и SUCCESS
    }


    private static Stream<Arguments> provideJsonDataAndExpectedCount() {
        return Stream.of(
                Arguments.of("[{\"name\": \"City1\"}, {\"name\": \"City2\"}]", 2),
                Arguments.of("[{\"name\": \"City1\"}]", 1),
                Arguments.of("[]", 0)
        );
    }

    @Test
    @DisplayName("Успешное получение истории операций импорта")
    void shouldGetImportHistorySuccessfully() {
        // Arrange
        ImportOperation successOperation = new ImportOperation();
        successOperation.setId(2L);
        successOperation.setStatus(ImportStatus.SUCCESS);
        successOperation.setTimestamp(LocalDateTime.now());
        successOperation.setObjectsAdded(5);
        successOperation.setUser(testUser);

        List<ImportOperation> operations = List.of(importOperation, successOperation);

        ImportOperationDTO dto1 = new ImportOperationDTO(
                1L,
                ImportStatus.SUCCESS,
                successOperation.getTimestamp(),
                3,
                testUser.getUsername()
        );

        ImportOperationDTO dto2 = new ImportOperationDTO(
                2L,
                ImportStatus.SUCCESS,
                successOperation.getTimestamp(),
                5,
                testUser.getUsername()
        );

        when(importOperationRepository.findByStatus(ImportStatus.SUCCESS)).thenReturn(operations);
        when(entityMapper.toImportOperationDTO(importOperation)).thenReturn(dto1);
        when(entityMapper.toImportOperationDTO(successOperation)).thenReturn(dto2);

        // Act
        List<ImportOperationDTO> result = importService.getImportHistory();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(importOperationRepository).findByStatus(ImportStatus.SUCCESS);
        verify(entityMapper, times(2)).toImportOperationDTO(any(ImportOperation.class));
    }

    @Test
    @DisplayName("Пустая история операций импорта")
    void shouldReturnEmptyHistoryWhenNoSuccessfulOperations() {
        // Arrange
        when(importOperationRepository.findByStatus(ImportStatus.SUCCESS)).thenReturn(Collections.emptyList());

        // Act
        List<ImportOperationDTO> result = importService.getImportHistory();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Результат должен быть пустым для отсутствующих успешных операций");
        verify(importOperationRepository).findByStatus(ImportStatus.SUCCESS);
        verifyNoInteractions(entityMapper);
    }
}