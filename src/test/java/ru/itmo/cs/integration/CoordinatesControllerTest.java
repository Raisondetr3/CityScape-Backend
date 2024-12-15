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
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.repository.CoordinatesRepository;
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
class CoordinatesControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User defaultUser;
    private Coordinates defaultCoordinates;

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

        defaultCoordinates = new Coordinates();
        defaultCoordinates.setX(100L);
        defaultCoordinates.setY(50.0);
        defaultCoordinates.setCreatedBy(defaultUser);
        defaultCoordinates = coordinatesRepository.save(defaultCoordinates);
    }

    @AfterEach
    void tearDown() {
        coordinatesRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение всех координат с пагинацией")
    void shouldGetAllCoordinatesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/coordinates")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(defaultCoordinates.getId()))
                .andExpect(jsonPath("$.content[0].x").value(defaultCoordinates.getX()))
                .andExpect(jsonPath("$.content[0].y").value(defaultCoordinates.getY()));
    }

    @Test
    @DisplayName("Успешное получение координат по ID")
    void shouldGetCoordinatesByIdSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/coordinates/{id}", defaultCoordinates.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultCoordinates.getId()))
                .andExpect(jsonPath("$.x").value(defaultCoordinates.getX()))
                .andExpect(jsonPath("$.y").value(defaultCoordinates.getY()));
    }

    @Test
    @DisplayName("Ошибка при запросе координат по несуществующему ID")
    void shouldFailToGetCoordinatesByNonExistentId() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/coordinates/{id}", 9999L)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Успешное создание координат")
    void shouldCreateCoordinatesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        CoordinatesDTO newCoordinates = new CoordinatesDTO(null, 200L, 100.5, null);

        MvcResult result = mockMvc.perform(post("/api/coordinates")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCoordinates)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        CoordinatesDTO createdCoordinates = objectMapper.readValue(response, CoordinatesDTO.class);

        assertThat(createdCoordinates.getId()).isNotNull();
        assertThat(createdCoordinates.getX()).isEqualTo(newCoordinates.getX());
        assertThat(createdCoordinates.getY()).isEqualTo(newCoordinates.getY());
    }

    @Test
    @DisplayName("Ошибка при создании координат с некорректным значением X")
    void shouldFailToCreateCoordinatesWithInvalidX() throws Exception {
        String token = generateToken(defaultUser);

        CoordinatesDTO invalidCoordinates = new CoordinatesDTO(null, 1000L, 100.5, null); // X > 820

        mockMvc.perform(post("/api/coordinates")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCoordinates)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешное обновление координат")
    void shouldUpdateCoordinatesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        CoordinatesDTO updatedCoordinates = new CoordinatesDTO(defaultCoordinates.getId(), 300L, 200.0, null);

        mockMvc.perform(put("/api/coordinates/{id}", defaultCoordinates.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCoordinates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultCoordinates.getId()))
                .andExpect(jsonPath("$.x").value(updatedCoordinates.getX()))
                .andExpect(jsonPath("$.y").value(updatedCoordinates.getY()));
    }

    @Test
    @DisplayName("Ошибка при обновлении координат с несуществующим ID")
    void shouldFailToUpdateCoordinatesWithNonExistentId() throws Exception {
        String token = generateToken(defaultUser);

        CoordinatesDTO updatedCoordinates = new CoordinatesDTO(9999L, 300L, 200.0, null);

        mockMvc.perform(put("/api/coordinates/{id}", 9999L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCoordinates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Успешное удаление координат")
    void shouldDeleteCoordinatesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(delete("/api/coordinates/{id}", defaultCoordinates.getId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assertThat(coordinatesRepository.findById(defaultCoordinates.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ошибка при удалении координат, связанных с городом")
    void shouldFailToDeleteCoordinatesLinkedToCity() throws Exception {
        String token = generateToken(defaultUser);

        Human governor = new Human();
        governor.setName("Test Governor");
        governor.setAge(45);
        governor.setHeight(175);
        governor.setCreatedBy(defaultUser);
        governor = humanRepository.save(governor);

        City city = new City();
        city.setName("Test City");
        city.setCoordinates(defaultCoordinates);
        city.setArea(100.0);
        city.setPopulation(1000L);
        city.setCapital(false);
        city.setGovernment(Government.JUNTA);
        city.setGovernor(governor);
        city.setCreatedBy(defaultUser);
        city = cityRepository.save(city);

        defaultCoordinates.getCities().add(city);
        coordinatesRepository.save(defaultCoordinates);

        assertThat(defaultCoordinates.getCities()).contains(city);

        mockMvc.perform(delete("/api/coordinates/{id}", defaultCoordinates.getId())
                        .header("Authorization", token))
                .andExpect(status().isConflict());
    }
}