package ru.itmo.cs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.entity.ImportOperation;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.*;
import ru.itmo.cs.repository.ImportOperationRepository;
import ru.itmo.cs.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImportControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImportOperationRepository importOperationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        when(jwtService.generateToken(any())).thenReturn("mockedToken");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);
        when(passwordEncoder.encode(any()))
                .thenAnswer(invocation -> "encoded" + invocation.getArgument(0));
        when(passwordEncoder.matches(any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0).equals(invocation.getArgument(1)
                        .toString().replace("encoded", ""))
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(defaultUser);

        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setPassword("encodedPassword");
        defaultUser.setRole(UserRole.USER);
        defaultUser = userRepository.save(defaultUser);
    }

    @AfterEach
    void tearDown() {
        importOperationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешный импорт городов из JSON-файла")
    void shouldImportCitiesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        // Arrange
        String jsonData = objectMapper.writeValueAsString(List.of(
                new CityDTO(
                        null, "New City", 200.0, 2000L, Climate.OCEANIC, Government.JUNTA,
                        new CoordinatesDTO(null, 200L, 200.5, null), true,
                        100L, StandardOfLiving.HIGH, null,
                        new HumanDTO(null, "New Governor", 50,
                                185, null, null), null, null
                )
        ));

        MockMultipartFile file = new MockMultipartFile(
                "file", "cities.json", MediaType.APPLICATION_JSON_VALUE, jsonData.getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/import")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.objectsAdded").value(1));

        assertThat(importOperationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Обработка пустого JSON-файла")
    void shouldHandleEmptyJsonFile() throws Exception {
        String token = generateToken(defaultUser);

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.json", MediaType.APPLICATION_JSON_VALUE, "[]".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/import")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.objectsAdded").value(0));

        assertThat(importOperationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Обработка ошибки при чтении файла")
    void shouldHandleFileReadError() throws Exception {
        String token = generateToken(defaultUser);

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.json", MediaType.APPLICATION_JSON_VALUE, new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/import")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка чтения файла: файл пустой"));
    }

    @Test
    @DisplayName("Ошибка при неправильной структуре JSON")
    void shouldHandleInvalidJsonStructure() throws Exception {
        String token = generateToken(defaultUser);

        String invalidJson = "[{\"invalidField\": \"invalidValue\"}]";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.json", MediaType.APPLICATION_JSON_VALUE, invalidJson.getBytes()
        );

        mockMvc.perform(multipart("/api/import")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ошибка парсинга JSON")));

        List<ImportOperation> operations = importOperationRepository.findAll();
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0).getStatus()).isEqualTo(ImportStatus.FAILURE);
    }

    @Test
    @DisplayName("Успешное получение истории операций импорта")
    void shouldGetImportHistorySuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        // Arrange
        ImportOperation operation1 = new ImportOperation();
        operation1.setStatus(ImportStatus.SUCCESS);
        operation1.setTimestamp(LocalDateTime.now().minusDays(1));
        operation1.setObjectsAdded(3);
        operation1.setUser(defaultUser);

        ImportOperation operation2 = new ImportOperation();
        operation2.setStatus(ImportStatus.SUCCESS);
        operation2.setTimestamp(LocalDateTime.now());
        operation2.setObjectsAdded(5);
        operation2.setUser(defaultUser);

        importOperationRepository.saveAll(List.of(operation1, operation2));

        // Act & Assert
        mockMvc.perform(get("/api/import/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].objectsAdded").value(3))
                .andExpect(jsonPath("$[0].username").value(defaultUser.getUsername()))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].objectsAdded").value(5))
                .andExpect(jsonPath("$[1].username").value(defaultUser.getUsername()));
    }

    @Test
    @DisplayName("Пустая история операций импорта")
    void shouldReturnEmptyImportHistory() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/import/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

}

