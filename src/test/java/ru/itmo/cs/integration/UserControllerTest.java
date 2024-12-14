package ru.itmo.cs.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.itmo.cs.dto.*;
import ru.itmo.cs.dto.auth.*;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                invocation.getArgument(0).equals(invocation.getArgument(1).toString().replace("encoded", ""))
        );

        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setPassword("encodedPassword");
        defaultUser.setRole(UserRole.USER);
        defaultUser.setAdminRequestStatus(AdminRequestStatus.NONE);
        defaultUser = userRepository.save(defaultUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO("newUser", "123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully."));

        Optional<User> createdUser = userRepository.findByUsername("newUser");
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getUsername()).isEqualTo("newUser");
    }

    @Test
    @DisplayName("Ошибка при регистрации пользователя с уже существующим username")
    void shouldFailRegistrationIfUserAlreadyExists() throws Exception {
        // Arrange
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO("testUser", "123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Успешная авторизация пользователя")
    void shouldLoginUserSuccessfully() throws Exception {
        UserRegistrationDTO regDto = new UserRegistrationDTO("loginUser", "123456");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isOk());

        UserLoginDTO loginDTO = new UserLoginDTO("loginUser", "123456");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthLoginResponseDTO authResponse = objectMapper.readValue(response, AuthLoginResponseDTO.class);
        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(authResponse.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Ошибка при авторизации с неправильным паролем")
    void shouldFailLoginWithWrongPassword() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO("testUser", "wrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Проверка наличия админа при его отсутствии")
    void shouldCheckAdminExistsWhenNoAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/admin-exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Запрос на роль админа при отсутствии других админов и моментальном повышении")
    void shouldRequestAdminApprovalAndBecomeAdminImmediately() throws Exception {
        String token = generateToken(defaultUser);

        AdminApprovalDTO approvalDTO = new AdminApprovalDTO(defaultUser.getId());
        mockMvc.perform(post("/api/auth/request-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalDTO))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("В системе нет администраторов. Пользователю были немедленно предоставлены права ADMIN"));

        User updatedUser = userRepository.findById(defaultUser.getId()).get();
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(updatedUser.getAdminRequestStatus()).isEqualTo(AdminRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Получение текущего пользователя")
    void shouldGetCurrentUser() throws Exception {
        String token = generateToken(defaultUser);

        MvcResult result = mockMvc.perform(get("/api/auth/current")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        UserDTO userDTO = objectMapper.readValue(response, UserDTO.class);

        assertThat(userDTO.getId()).isEqualTo(defaultUser.getId());
        assertThat(userDTO.getUsername()).isEqualTo(defaultUser.getUsername());
        assertThat(userDTO.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Обновление текущего пользователя: изменение username")
    void shouldUpdateCurrentUserUsername() throws Exception {
        String token = generateToken(defaultUser);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updatedUsername");

        MvcResult result = mockMvc.perform(put("/api/auth/current")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        UserUpdateResponseDTO userUpdateResponseDTO = objectMapper.readValue(response, UserUpdateResponseDTO.class);

        assertThat(userUpdateResponseDTO.getToken()).isNotBlank();
        assertThat(userUpdateResponseDTO.getUser().getUsername()).isEqualTo("updatedUsername");

        User updatedUser = userRepository.findById(defaultUser.getId()).get();
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUsername");
    }

    @Test
    @DisplayName("Получение статуса запроса на роль админа для пользователя, которого нет")
    void shouldFailToGetAdminRequestStatusForNonExistentUser() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/auth/admin-requests/status")
                        .param("userId", "9999") // несуществующий пользователь
                        .header("Authorization", token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Получение запросов на роль админа под учеткой админа")
    void shouldGetAdminRequestsAsAdmin() throws Exception {
        User admin = new User();
        admin.setUsername("adminUser");
        admin.setPassword("encodedPassword");
        admin.setRole(UserRole.ADMIN);
        admin.setAdminRequestStatus(AdminRequestStatus.NONE);
        admin = userRepository.save(admin);

        User userForRequest = new User();
        userForRequest.setUsername("userForRequest");
        userForRequest.setPassword("encodedPassword");
        userForRequest.setRole(UserRole.USER);
        userForRequest.setAdminRequestStatus(AdminRequestStatus.PENDING);
        userForRequest = userRepository.save(userForRequest);

        String token = generateToken(admin);

        MvcResult result = mockMvc.perform(get("/api/auth/admin-requests")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        List<UserDTO> requests = objectMapper.readValue(response, new TypeReference<List<UserDTO>>(){});

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getUsername()).isEqualTo("userForRequest");
        assertThat(requests.get(0).getAdminRequestStatus()).isEqualTo(AdminRequestStatus.PENDING);
    }
}
