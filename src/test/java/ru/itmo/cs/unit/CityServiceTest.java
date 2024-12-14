package ru.itmo.cs.unit;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.dto.city.CityFilterCriteria;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.service.*;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @InjectMocks
    private CityService cityService;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CoordinatesService coordinatesService;

    @Mock
    private HumanService humanService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private FilterProcessor<CityDTO, CityFilterCriteria> cityFilterProcessor;

    @Mock
    private PaginationHandler paginationHandler;

    @Mock
    private CalculateDistanceService calculateDistanceService;

    private CityDTO cityDTO;
    private City city;
    private CoordinatesDTO coordinatesDTO;
    private Coordinates coordinates;
    private HumanDTO humanDTO;
    private Human human;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        coordinatesDTO = new CoordinatesDTO(1L, 100L, 200.5, user);
        coordinates = new Coordinates();
        coordinates.setId(1L);
        coordinates.setX(100L);
        coordinates.setY(200.5);

        humanDTO = new HumanDTO(1L, "John Doe", 30, 180, ZonedDateTime.now(), user);
        human = new Human();
        human.setId(1L);
        human.setName("John Doe");
        human.setAge(30);

        cityDTO = new CityDTO(
                1L,
                "Test City",
                1000.0,
                5000L,
                Climate.STEPPE,
                Government.REPUBLIC,
                coordinatesDTO,
                false,
                50L,
                StandardOfLiving.HIGH,
                LocalDateTime.now(),
                humanDTO,
                user,
                LocalDate.now()
        );

        city = new City();
        city.setId(1L);
        city.setName("Test City");
        city.setCoordinates(coordinates);
        city.setGovernor(human);
        city.setCreatedBy(user);
        city.setArea(1000.0);
        city.setPopulation(5000L);
    }


    @Test
    @DisplayName("Успешное создание города")
    void shouldCreateCitySuccessfully() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(user);
        when(coordinatesService.createOrUpdateCoordinatesForCity(coordinatesDTO)).thenReturn(coordinates);
        when(humanService.createOrUpdateHumanForCity(humanDTO)).thenReturn(human);
        when(entityMapper.toCityEntity(cityDTO, coordinates, human)).thenReturn(city);
        when(cityRepository.save(city)).thenReturn(city);
        when(entityMapper.toCityDTO(city)).thenReturn(cityDTO);

        // Act
        CityDTO result = cityService.createCity(cityDTO);

        // Assert
        assertNotNull(result);
        assertEquals(cityDTO, result);
        verify(auditService).auditCity(city, AuditOperation.CREATE);
    }

    @Test
    @DisplayName("Ошибка при попытке обновления города без прав")
    void shouldThrowExceptionWhenUpdatingCityWithoutPermission() {
        // Arrange
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(userService.canModifyCity(city)).thenReturn(false);

        // Act & Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> cityService.updateCity(1L, cityDTO),
                "Ожидалось исключение при отсутствии прав на обновление города"
        );

        assertEquals("У вас нет разрешения на изменение этого City", exception.getMessage());
        verify(cityRepository).findById(1L);
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Успешное удаление города")
    void shouldDeleteCitySuccessfully() {
        // Arrange
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(userService.canModifyCity(city)).thenReturn(true);

        // Act
        cityService.deleteCity(1L);

        // Assert
        verify(auditService).deleteCityAuditEntries(1L);
        verify(cityRepository).delete(city);
    }

    @Test
    @DisplayName("Ошибка при удалении города без прав")
    void shouldThrowExceptionWhenDeletingCityWithoutPermission() {
        // Arrange
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(userService.canModifyCity(city)).thenReturn(false);

        // Act & Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> cityService.deleteCity(1L),
                "Ожидалось исключение при отсутствии прав на удаление города"
        );

        assertEquals("У вас нет разрешения на удаление этого City", exception.getMessage());
        verify(cityRepository).findById(1L);
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Успешный расчет маршрута к городу с наибольшей площадью")
    void shouldCalculateRouteToCityWithLargestArea() {
        // Arrange
        when(cityRepository.findTopByOrderByAreaDesc()).thenReturn(city);
        when(calculateDistanceService.calculate(0, 0, 0, 100, 200.5, 0)).thenReturn(223.61);

        // Act
        double distance = cityService.calculateRouteToCityWithLargestArea();

        // Assert
        assertEquals(223.61, distance);
    }

    @Test
    @DisplayName("Ошибка при расчете маршрута, если города отсутствуют")
    void shouldThrowExceptionWhenNoCitiesExist() {
        // Arrange
        when(cityRepository.findTopByOrderByAreaDesc()).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> cityService.calculateRouteToCityWithLargestArea(),
                "Ожидалось исключение при отсутствии городов"
        );

        assertEquals("No cities found", exception.getMessage());
    }

    @ParameterizedTest
    @DisplayName("Параметризованный тест для расчета маршрута к городу с наибольшей площадью")
    @CsvSource({
            "0, 0, 0, 100, 200.5, 223.61",
            "50, 50, 0, 150, 250.5, 150.0"
    })
    void shouldCalculateRouteToCityWithLargestAreaFromUserParameterized(
            double userX, double userY, double userZ,
            double cityX, double cityY, double expectedDistance) {
        // Arrange
        when(cityRepository.findTopByOrderByAreaDesc()).thenReturn(city);
        lenient().when(calculateDistanceService.calculate(anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble()))
                    .thenReturn(expectedDistance);


        // Act
        double distance = cityService.calculateRouteToCityWithLargestAreaFromUser(userX, userY, userZ);

        // Assert
        assertEquals(expectedDistance, distance);
    }

    @Test
    @DisplayName("Успешное получение города по ID")
    void shouldGetCityByIdSuccessfully() {
        // Arrange
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(entityMapper.toCityDTO(city)).thenReturn(cityDTO);

        // Act
        CityDTO result = cityService.getCityById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(cityDTO, result);
    }

    @Test
    @DisplayName("Ошибка при попытке получить город по несуществующему ID")
    void shouldThrowExceptionWhenCityNotFoundById() {
        // Arrange
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cityService.getCityById(1L),
                "Ожидалось исключение при отсутствии города с данным ID"
        );

        assertEquals("City не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Успешное удаление города по правительству")
    void shouldDeleteCityByGovernmentSuccessfully() {
        // Arrange
        when(cityRepository.findFirstByGovernment(Government.REPUBLIC)).thenReturn(Optional.of(city));
        when(userService.canModifyCity(city)).thenReturn(true);

        // Act
        cityService.deleteCityByGovernment(Government.REPUBLIC);

        // Assert
        verify(auditService).deleteCityAuditEntries(1L);
        verify(cityRepository).delete(city);
    }

    @Test
    @DisplayName("Ошибка при удалении города по правительству без прав")
    void shouldThrowExceptionWhenDeletingCityByGovernmentWithoutPermission() {
        // Arrange
        when(cityRepository.findFirstByGovernment(Government.REPUBLIC)).thenReturn(Optional.of(city));
        when(userService.canModifyCity(city)).thenReturn(false);

        // Act & Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> cityService.deleteCityByGovernment(Government.REPUBLIC),
                "Ожидалось исключение при отсутствии прав на удаление"
        );

        assertEquals("You don't have permission to delete this city", exception.getMessage());
    }

    @Test
    @DisplayName("Успешный расчет общего числа метров над уровнем моря")
    void shouldCalculateTotalMetersAboveSeaLevelSuccessfully() {
        // Arrange
        when(cityRepository.sumMetersAboveSeaLevel()).thenReturn(1500L);

        // Act
        Long result = cityService.calculateTotalMetersAboveSeaLevel();

        // Assert
        assertEquals(1500L, result);
    }

    @Test
    @DisplayName("Успешный подсчет городов по климату")
    void shouldCountCitiesByClimateSuccessfully() {
        // Arrange
        when(cityRepository.findByClimateGreaterThan(Climate.STEPPE)).thenReturn(List.of(city));

        // Act
        Long result = cityService.countCitiesByClimate(Climate.STEPPE);

        // Assert
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("Успешная фильтрация городов")
    void shouldFilterCitiesSuccessfully() {
        // Arrange
        CityFilterCriteria criteria = new CityFilterCriteria();
        criteria.setName("Test");
        criteria.setClimate(Climate.STEPPE);
        Page<CityDTO> expectedPage = new PageImpl<>(List.of(cityDTO));

        when(paginationHandler.createPageable(0, 10, "name", "asc")).thenReturn(PageRequest.of(0, 10, Sort.by("name")));
        when(cityFilterProcessor.filter(criteria, PageRequest.of(0, 10, Sort.by("name"))))
                .thenReturn(expectedPage);

        // Act
        Page<CityDTO> result = cityService.getAllCities("Test", Climate.STEPPE, null, null, null, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage, result);
    }
}
