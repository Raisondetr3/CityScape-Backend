package ru.itmo.cs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.exception.EntityDeletionException;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.CoordinatesRepository;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.service.CoordinatesService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoordinatesServiceTest {

    @InjectMocks
    private CoordinatesService coordinatesService;

    @Mock
    private CoordinatesRepository coordinatesRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private UserService userService;

    @Mock
    private PaginationHandler paginationHandler;

    private Coordinates coordinates;
    private CoordinatesDTO coordinatesDTO;
    private User user;

    @BeforeEach
    void setUp() {
        coordinates = new Coordinates();
        coordinates.setId(1L);
        coordinates.setX(100L);
        coordinates.setY(200.0);

        coordinatesDTO = new CoordinatesDTO();
        coordinatesDTO.setId(1L);
        coordinatesDTO.setX(100L);
        coordinatesDTO.setY(200.0);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
    }

    @Test
    @DisplayName("Успешное получение всех координат с пагинацией")
    void shouldGetAllCoordinatesSuccessfully() {
        // Arrange
        Page<Coordinates> coordinatesPage = new PageImpl<>(List.of(coordinates));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("x").ascending());
        when(paginationHandler.createPageable(0, 10, "x", "asc")).thenReturn(pageable);
        when(coordinatesRepository.findAll(pageable)).thenReturn(coordinatesPage);
        when(entityMapper.toCoordinatesDTO(coordinates)).thenReturn(coordinatesDTO);

        // Act
        Page<CoordinatesDTO> result = coordinatesService.getAllCoordinates(0, 10, "x", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(coordinatesDTO, result.getContent().get(0));
        verify(paginationHandler).createPageable(0, 10, "x", "asc");
        verify(coordinatesRepository).findAll(pageable);
        verify(entityMapper).toCoordinatesDTO(coordinates);
    }

    @Test
    @DisplayName("Успешное получение координат по ID")
    void shouldGetCoordinatesByIdSuccessfully() {
        // Arrange
        when(coordinatesRepository.findById(1L)).thenReturn(Optional.of(coordinates));
        when(entityMapper.toCoordinatesDTO(coordinates)).thenReturn(coordinatesDTO);

        // Act
        CoordinatesDTO result = coordinatesService.getCoordinatesById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(coordinatesDTO, result);
        verify(coordinatesRepository).findById(1L);
        verify(entityMapper).toCoordinatesDTO(coordinates);
    }

    @Test
    @DisplayName("Ошибка при получении координат по несуществующему ID")
    void shouldThrowExceptionWhenCoordinatesNotFoundById() {
        // Arrange
        when(coordinatesRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> coordinatesService.getCoordinatesById(1L),
                "Ожидалось исключение для отсутствующих координат"
        );

        assertEquals("Coordinates не найден", exception.getMessage());
        verify(coordinatesRepository).findById(1L);
    }

    @Test
    @DisplayName("Успешное создание координат")
    void shouldCreateCoordinatesSuccessfully() {
        // Arrange
        when(entityMapper.toCoordinatesEntity(coordinatesDTO)).thenReturn(coordinates);
        when(userService.getCurrentUser()).thenReturn(user);
        when(coordinatesRepository.save(coordinates)).thenReturn(coordinates);
        when(entityMapper.toCoordinatesDTO(coordinates)).thenReturn(coordinatesDTO);

        // Act
        CoordinatesDTO result = coordinatesService.createCoordinates(coordinatesDTO);

        // Assert
        assertNotNull(result);
        assertEquals(coordinatesDTO, result);
        verify(entityMapper).toCoordinatesEntity(coordinatesDTO);
        verify(userService).getCurrentUser();
        verify(coordinatesRepository).save(coordinates);
        verify(auditService).auditCoordinates(coordinates, AuditOperation.CREATE);
        verify(entityMapper).toCoordinatesDTO(coordinates);
    }

    @Test
    @DisplayName("Успешное обновление координат")
    void shouldUpdateCoordinatesSuccessfully() {
        // Arrange
        when(coordinatesRepository.findById(coordinatesDTO.getId())).thenReturn(Optional.of(coordinates));
        when(coordinatesRepository.save(coordinates)).thenReturn(coordinates);
        when(entityMapper.toCoordinatesDTO(coordinates)).thenReturn(coordinatesDTO);

        // Act
        CoordinatesDTO result = coordinatesService.updateCoordinates(coordinatesDTO);

        // Assert
        assertNotNull(result);
        assertEquals(coordinatesDTO, result);
        verify(coordinatesRepository).findById(coordinatesDTO.getId());
        verify(coordinatesRepository).save(coordinates);
        verify(auditService).auditCoordinates(coordinates, AuditOperation.UPDATE);
        verify(entityMapper).toCoordinatesDTO(coordinates);
    }

    @Test
    @DisplayName("Ошибка при обновлении несуществующих координат")
    void shouldThrowExceptionWhenUpdatingNonExistingCoordinates() {
        // Arrange
        when(coordinatesRepository.findById(coordinatesDTO.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> coordinatesService.updateCoordinates(coordinatesDTO),
                "Ожидалось исключение для отсутствующих координат"
        );

        assertEquals("Coordinates не найден", exception.getMessage());
        verify(coordinatesRepository).findById(coordinatesDTO.getId());
    }

    @Test
    @DisplayName("Успешное удаление координат")
    void shouldDeleteCoordinatesSuccessfully() {
        // Arrange
        Coordinates mockedCoordinates = mock(Coordinates.class); // Создаём мок-объект Coordinates
        when(mockedCoordinates.getId()).thenReturn(1L); // Указываем, что getId() возвращает 1L
        when(mockedCoordinates.getCities()).thenReturn(Collections.emptyList()); // Указываем, что getCities() возвращает пустой список
        when(coordinatesRepository.findById(1L)).thenReturn(Optional.of(mockedCoordinates));

        // Act
        coordinatesService.deleteCoordinates(1L);

        // Assert
        verify(coordinatesRepository).findById(1L); // Проверяем, что репозиторий вызван для поиска координат
        verify(auditService).deleteCoordinatesAuditEntries(1L); // Проверяем, что вызван метод удаления записей аудита
        verify(coordinatesRepository).delete(mockedCoordinates); // Проверяем, что объект был удалён
    }


    @Test
    @DisplayName("Ошибка при удалении координат, связанных с городами")
    void shouldThrowExceptionWhenDeletingCoordinatesLinkedToCities() {
        // Arrange
        Coordinates mockedCoordinates = mock(Coordinates.class);
        when(coordinatesRepository.findById(1L)).thenReturn(Optional.of(mockedCoordinates));
        when(mockedCoordinates.getCities()).thenReturn(List.of(new City()));

        // Act & Assert
        EntityDeletionException exception = assertThrows(
                EntityDeletionException.class,
                () -> coordinatesService.deleteCoordinates(1L),
                "Ожидалось исключение при удалении связанных координат"
        );

        assertEquals("Невозможно удалить Coordinates, поскольку они связаны с одним или несколькими Cities", exception.getMessage());
        verify(coordinatesRepository).findById(1L);
        verifyNoInteractions(auditService);
        verifyNoMoreInteractions(coordinatesRepository);
    }
}