package ru.itmo.cs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.audit.CityAudit;
import ru.itmo.cs.entity.audit.CoordinatesAudit;
import ru.itmo.cs.entity.audit.HumanAudit;
import ru.itmo.cs.repository.audit.CityAuditRepository;
import ru.itmo.cs.repository.audit.CoordinatesAuditRepository;
import ru.itmo.cs.repository.audit.HumanAuditRepository;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.util.EntityMapper;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private CityAuditRepository cityAuditRepository;

    @Mock
    private HumanAuditRepository humanAuditRepository;

    @Mock
    private CoordinatesAuditRepository coordinatesAuditRepository;

    @Mock
    private EntityMapper entityMapper;

    private City city;
    private Human human;
    private Coordinates coordinates;
    private CityAudit cityAudit;
    private HumanAudit humanAudit;
    private CoordinatesAudit coordinatesAudit;

    @BeforeEach
    void setUp() {
        city = new City();
        city.setId(1L);
        city.setName("Test City");

        human = new Human();
        human.setId(1L);
        human.setName("Test Human");

        coordinates = new Coordinates();
        coordinates.setId(1L);
        coordinates.setX(100L);
        coordinates.setY(200.0);

        cityAudit = new CityAudit();
        cityAudit.setId(1L);
        cityAudit.setCity(city);
        cityAudit.setOperation(AuditOperation.CREATE);

        humanAudit = new HumanAudit();
        humanAudit.setId(1L);
        humanAudit.setHuman(human);
        humanAudit.setOperation(AuditOperation.UPDATE);

        coordinatesAudit = new CoordinatesAudit();
        coordinatesAudit.setId(1L);
        coordinatesAudit.setCoordinates(coordinates);
        coordinatesAudit.setOperation(AuditOperation.CREATE);
    }

    @Test
    @DisplayName("Успешный аудит города")
    void shouldAuditCitySuccessfully() {
        // Arrange
        when(entityMapper.toCityAudit(city, AuditOperation.CREATE)).thenReturn(cityAudit);
        when(cityAuditRepository.save(cityAudit)).thenReturn(cityAudit);

        // Act
        auditService.auditCity(city, AuditOperation.CREATE);

        // Assert
        verify(entityMapper).toCityAudit(city, AuditOperation.CREATE);
        verify(cityAuditRepository).save(cityAudit);
    }

    @Test
    @DisplayName("Успешный аудит человека")
    void shouldAuditHumanSuccessfully() {
        // Arrange
        when(entityMapper.toHumanAudit(human, AuditOperation.UPDATE)).thenReturn(humanAudit);
        when(humanAuditRepository.save(humanAudit)).thenReturn(humanAudit);

        // Act
        auditService.auditHuman(human, AuditOperation.UPDATE);

        // Assert
        verify(entityMapper).toHumanAudit(human, AuditOperation.UPDATE);
        verify(humanAuditRepository).save(humanAudit);
    }

    @Test
    @DisplayName("Успешный аудит координат")
    void shouldAuditCoordinatesSuccessfully() {
        // Arrange
        when(entityMapper.toCoordinatesAudit(coordinates, AuditOperation.CREATE)).thenReturn(coordinatesAudit);
        when(coordinatesAuditRepository.save(coordinatesAudit)).thenReturn(coordinatesAudit);

        // Act
        auditService.auditCoordinates(coordinates, AuditOperation.CREATE);

        // Assert
        verify(entityMapper).toCoordinatesAudit(coordinates, AuditOperation.CREATE);
        verify(coordinatesAuditRepository).save(coordinatesAudit);
    }

    @Test
    @DisplayName("Удаление записей аудита города")
    void shouldDeleteCityAuditEntries() {
        // Arrange
        Long cityId = 1L;

        // Act
        auditService.deleteCityAuditEntries(cityId);

        // Assert
        verify(cityAuditRepository).deleteAllByCityId(cityId);
    }

    @Test
    @DisplayName("Удаление записей аудита человека")
    void shouldDeleteHumanAuditEntries() {
        // Arrange
        Long humanId = 1L;

        // Act
        auditService.deleteHumanAuditEntries(humanId);

        // Assert
        verify(humanAuditRepository).deleteAllByHumanId(humanId);
    }

    @Test
    @DisplayName("Удаление записей аудита координат")
    void shouldDeleteCoordinatesAuditEntries() {
        // Arrange
        Long coordinatesId = 1L;

        // Act
        auditService.deleteCoordinatesAuditEntries(coordinatesId);

        // Assert
        verify(coordinatesAuditRepository).deleteAllByCoordinatesId(coordinatesId);
    }
}
