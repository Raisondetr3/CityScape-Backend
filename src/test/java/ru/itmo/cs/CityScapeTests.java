package ru.itmo.cs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.itmo.cs.service.JwtService;

import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class CityScapeTests {

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUpMocks() {
        when(jwtService.getSecretKey()).thenReturn("H++QPACG3PMKlsKILieJCXpW0DrJUMpiPTcU/KosFYU=");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
    }

    @BeforeAll
    public static void setUp() {
        // Database configuration
        System.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        System.setProperty("spring.datasource.username", "sa");
        System.setProperty("spring.datasource.password", "");
        System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");

        // Hibernate dialect for H2 emulating PostgreSQL
        System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        // JWT configuration
        System.setProperty("security.jwt.secret-key", "H++QPACG3PMKlsKILieJCXpW0DrJUMpiPTcU/KosFYU=");
        System.setProperty("security.jwt.expiration-time", "3600000");
    }

    @Test
    void contextLoads() {
    }
}
