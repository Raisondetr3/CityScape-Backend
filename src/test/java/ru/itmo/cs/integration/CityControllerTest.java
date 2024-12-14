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
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.*;
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
class CityControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User defaultUser;
    private Coordinates defaultCoordinates;
    private Human defaultGovernor;
    private City defaultCity;

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
        defaultUser.setAdminRequestStatus(AdminRequestStatus.NONE);
        defaultUser = userRepository.save(defaultUser);

        defaultCoordinates = new Coordinates();
        defaultCoordinates.setX(100L);
        defaultCoordinates.setY(50.0);
        defaultCoordinates.setCreatedBy(defaultUser);
        defaultCoordinates = coordinatesRepository.save(defaultCoordinates);

        defaultGovernor = new Human();
        defaultGovernor.setName("Default Governor");
        defaultGovernor.setAge(45);
        defaultGovernor.setHeight(180);
        defaultGovernor.setCreatedBy(defaultUser);
        defaultGovernor = humanRepository.save(defaultGovernor);

        defaultCity = new City();
        defaultCity.setName("Default City");
        defaultCity.setArea(100.0);
        defaultCity.setPopulation(1000L);
        defaultCity.setClimate(Climate.OCEANIC);
        defaultCity.setGovernment(Government.JUNTA);
        defaultCity.setCoordinates(defaultCoordinates);
        defaultCity.setGovernor(defaultGovernor);
        defaultCity.setCapital(true);
        defaultCity.setMetersAboveSeaLevel(50);
        defaultCity.setCreatedBy(defaultUser);
        defaultCity = cityRepository.save(defaultCity);
    }

    @AfterEach
    void tearDown() {
        cityRepository.deleteAll();
        humanRepository.deleteAll();
        coordinatesRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение всех городов с пагинацией")
    void shouldGetAllCitiesSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/cities")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(defaultCity.getId()))
                .andExpect(jsonPath("$.content[0].name").value(defaultCity.getName()));
    }

    @Test
    @DisplayName("Успешное получение города по ID")
    void shouldGetCityByIdSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/cities/{id}", defaultCity.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultCity.getId()))
                .andExpect(jsonPath("$.name").value(defaultCity.getName()));
    }

    @Test
    @DisplayName("Ошибка при запросе города по несуществующему ID")
    void shouldFailToGetCityByNonExistentId() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/cities/{id}", 9999L)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Успешное создание города")
    void shouldCreateCitySuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        CityDTO newCity = new CityDTO(
                null, "New City", 200.0, 2000L, Climate.OCEANIC, Government.JUNTA,
                new CoordinatesDTO(null, 200L, 200.5, null), true, 100L,
                StandardOfLiving.HIGH, null, new HumanDTO(null, "New Governor", 50,
                185, null, null), null, null
        );

        MvcResult result = mockMvc.perform(post("/api/cities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCity)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        CityDTO createdCity = objectMapper.readValue(response, CityDTO.class);

        assertThat(createdCity.getId()).isNotNull();
        assertThat(createdCity.getName()).isEqualTo(newCity.getName());
    }

    @Test
    @DisplayName("Ошибка при создании города с некорректной площадью")
    void shouldFailToCreateCityWithInvalidArea() throws Exception {
        String token = generateToken(defaultUser);

        CityDTO invalidCity = new CityDTO(
                null, "Invalid City", -10.0, 2000L, Climate.OCEANIC, Government.JUNTA,
                new CoordinatesDTO(null, 200L, 200.5, null), true, 100L,
                StandardOfLiving.HIGH,
                null, new HumanDTO(null, "New Governor", 50, 185,
                null, null), null, null
        );

        mockMvc.perform(post("/api/cities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCity)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешное обновление города")
    void shouldUpdateCitySuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        CityDTO updatedCity = new CityDTO(
                defaultCity.getId(), "Updated City", 300.0, 3000L, Climate.OCEANIC,
                Government.JUNTA, new CoordinatesDTO(defaultCoordinates.getId(), 300L, 300.5, null),
                false, 200L, StandardOfLiving.MEDIUM,
                null, new HumanDTO(defaultGovernor.getId(), "Updated Governor", 60,
                190, null, null), null, null
        );

        mockMvc.perform(put("/api/cities/{id}", defaultCity.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedCity.getName()))
                .andExpect(jsonPath("$.area").value(updatedCity.getArea()));
    }

    @Test
    @DisplayName("Успешное удаление города")
    void shouldDeleteCitySuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(delete("/api/cities/{id}", defaultCity.getId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assertThat(cityRepository.findById(defaultCity.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ошибка при удалении города, который вы не можете модифицировать")
    void shouldFailToDeleteCityWithoutPermission() throws Exception {
        String token = generateToken(defaultUser);

        User otherUser = new User();
        otherUser.setUsername("otherUser");
        otherUser.setPassword("encodedPassword");
        otherUser.setRole(UserRole.USER);
        otherUser.setAdminRequestStatus(AdminRequestStatus.NONE);
        otherUser = userRepository.save(otherUser);

        City unauthorizedCity = new City();
        unauthorizedCity.setName("Unauthorized City");
        unauthorizedCity.setArea(150.0);
        unauthorizedCity.setPopulation(1500L);
        unauthorizedCity.setClimate(Climate.OCEANIC);
        unauthorizedCity.setGovernment(Government.JUNTA);
        unauthorizedCity.setCoordinates(defaultCoordinates);
        unauthorizedCity.setGovernor(defaultGovernor);
        unauthorizedCity.setCapital(false);
        unauthorizedCity.setMetersAboveSeaLevel(20);
        unauthorizedCity.setCreatedBy(otherUser);
        unauthorizedCity = cityRepository.save(unauthorizedCity);

        mockMvc.perform(delete("/api/cities/{id}", unauthorizedCity.getId())
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}

