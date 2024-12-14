package ru.itmo.cs.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Rollback
@Transactional
public class IntegrationTestBase {

    @MockBean
    protected JwtService jwtService;

    @BeforeEach
    void setUpMocks() {
        when(jwtService.getSecretKey()).thenReturn("H++QPACG3PMKlsKILieJCXpW0DrJUMpiPTcU/KosFYU=");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
        when(jwtService.extractUsername("adminToken")).thenReturn("adminUser");
        when(jwtService.extractUsername("userToken")).thenReturn("testUser");
        when(jwtService.extractUsername("userToken2")).thenReturn("testUser2");
        when(jwtService.validateToken(anyString(), any())).thenReturn(true);
    }

    protected String generateToken(User user) {
        if (user.getUsername().equals("adminUser")) {
            return "Bearer adminToken";
        } else {
            return "Bearer userToken";
        }
    }
}