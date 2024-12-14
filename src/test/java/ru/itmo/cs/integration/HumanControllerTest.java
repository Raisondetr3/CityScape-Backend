package ru.itmo.cs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.repository.UserRepository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HumanControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User defaultUser;
    private Human defaultHuman;

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
        defaultUser.setId(1L);
        defaultUser.setUsername("testUser");
        defaultUser.setPassword("encodedPassword");
        defaultUser.setRole(UserRole.USER);
        defaultUser.setAdminRequestStatus(AdminRequestStatus.NONE);
        defaultUser = userRepository.save(defaultUser);

        defaultHuman = new Human();
        defaultHuman.setName("Default Human");
        defaultHuman.setAge(30);
        defaultHuman.setHeight(180);
        defaultHuman.setCreatedBy(defaultUser);
        defaultHuman = humanRepository.save(defaultHuman);
    }

    @AfterEach
    void tearDown() {
        cityRepository.deleteAll();
        humanRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение всех людей с пагинацией")
    void shouldGetAllHumansSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/humans")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(defaultHuman.getId()))
                .andExpect(jsonPath("$.content[0].name").value(defaultHuman.getName()));
    }

    @Test
    @DisplayName("Успешное получение человека по ID")
    void shouldGetHumanByIdSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/humans/{id}", defaultHuman.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultHuman.getId()))
                .andExpect(jsonPath("$.name").value(defaultHuman.getName()));
    }

    @Test
    @DisplayName("Ошибка при запросе человека по несуществующему ID")
    void shouldFailToGetHumanByNonExistentId() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/humans/{id}", 9999L)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Успешное создание человека")
    void shouldCreateHumanSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        HumanDTO newHuman = new HumanDTO(null, "New Human", 25, 170, null, null);

        MvcResult result = mockMvc.perform(post("/api/humans")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newHuman)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        HumanDTO createdHuman = objectMapper.readValue(response, HumanDTO.class);

        assertThat(createdHuman.getId()).isNotNull();
        assertThat(createdHuman.getName()).isEqualTo(newHuman.getName());
    }

    @Test
    @DisplayName("Ошибка при создании человека с некорректным именем")
    void shouldFailToCreateHumanWithInvalidName() throws Exception {
        String token = generateToken(defaultUser);

        HumanDTO invalidHuman = new HumanDTO(null, "", 25, 170, null, null); // Имя не должно быть пустым

        mockMvc.perform(post("/api/humans")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHuman)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешное обновление человека")
    void shouldUpdateHumanSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        HumanDTO updatedHuman = new HumanDTO(defaultHuman.getId(), "Updated Human", 35, 185, null, null);

        mockMvc.perform(put("/api/humans/{id}", defaultHuman.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedHuman)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultHuman.getId()))
                .andExpect(jsonPath("$.name").value(updatedHuman.getName()))
                .andExpect(jsonPath("$.age").value(updatedHuman.getAge()));
    }

    @Test
    @DisplayName("Ошибка при обновлении человека с несуществующим ID")
    void shouldFailToUpdateHumanWithNonExistentId() throws Exception {
        String token = generateToken(defaultUser);

        HumanDTO updatedHuman = new HumanDTO(9999L, "Updated Human", 35, 185, null, null);

        mockMvc.perform(put("/api/humans/{id}", 9999L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedHuman)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Успешное удаление человека")
    void shouldDeleteHumanSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(delete("/api/humans/{id}", defaultHuman.getId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assertThat(humanRepository.findById(defaultHuman.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ошибка при удалении человека, связанного с городом")
    void shouldFailToDeleteHumanLinkedToCity() throws Exception {
        String token = generateToken(defaultUser);

        City city = new City();
        city.setName("Test City");
        city.setGovernor(defaultHuman);
        city.setArea(100.0);
        city.setPopulation(1000L);
        city.setCapital(false);
        city.setGovernment(Government.JUNTA);
        city.setCreatedBy(defaultUser);
        cityRepository.save(city);

        defaultHuman.getCities().add(city);
        humanRepository.save(defaultHuman);

        mockMvc.perform(delete("/api/humans/{id}", defaultHuman.getId())
                        .header("Authorization", token))
                .andExpect(status().isConflict());
    }
}
