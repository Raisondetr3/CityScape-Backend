package ru.itmo.cs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.dto.human.HumanFilterCriteria;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.exception.EntityDeletionException;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.service.HumanService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HumanServiceTest {

    @InjectMocks
    private HumanService humanService;

    @Mock
    private HumanRepository humanRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UserService userService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private FilterProcessor<HumanDTO, HumanFilterCriteria> humanFilterProcessor;

    @Mock
    private PaginationHandler paginationHandler;

    private Human human;
    private HumanDTO humanDTO;
    private User user;

    @BeforeEach
    void setUp() {
        human = new Human();
        human.setId(1L);
        human.setName("John Doe");
        human.setAge(30);
        human.setHeight(180);

        humanDTO = new HumanDTO();
        humanDTO.setId(1L);
        humanDTO.setName("John Doe");
        humanDTO.setAge(30);
        humanDTO.setHeight(180);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
    }

    @Test
    @DisplayName("Успешное получение всех людей с фильтрацией и пагинацией")
    void shouldGetAllHumansSuccessfully() {
        // Arrange
        HumanFilterCriteria criteria = new HumanFilterCriteria();
        criteria.setName("John");

        Page<HumanDTO> humanDTOPage = new PageImpl<>(List.of(humanDTO));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        when(paginationHandler.createPageable(0, 10, "name", "asc")).thenReturn(pageable);
        when(humanFilterProcessor.filter(criteria, pageable)).thenReturn(humanDTOPage);

        // Act
        Page<HumanDTO> result = humanService.getAllHumans("John", 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(humanDTO, result.getContent().get(0));
        verify(humanFilterProcessor).filter(criteria, pageable);
    }

    @Test
    @DisplayName("Успешное получение человека по ID")
    void shouldGetHumanByIdSuccessfully() {
        // Arrange
        when(humanRepository.findById(1L)).thenReturn(Optional.of(human));
        when(entityMapper.toHumanDTO(human)).thenReturn(humanDTO);

        // Act
        HumanDTO result = humanService.getHumanById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(humanDTO, result);
        verify(humanRepository).findById(1L);
        verify(entityMapper).toHumanDTO(human);
    }

    @Test
    @DisplayName("Ошибка при получении человека по несуществующему ID")
    void shouldThrowExceptionWhenHumanNotFoundById() {
        // Arrange
        when(humanRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> humanService.getHumanById(1L),
                "Ожидалось исключение для отсутствующего человека"
        );

        assertEquals("Human не найден", exception.getMessage());
        verify(humanRepository).findById(1L);
    }

    @Test
    @DisplayName("Успешное создание человека")
    void shouldCreateHumanSuccessfully() {
        // Arrange
        when(entityMapper.toHumanEntity(humanDTO)).thenReturn(human);
        when(userService.getCurrentUser()).thenReturn(user);
        when(humanRepository.save(human)).thenReturn(human);
        when(entityMapper.toHumanDTO(human)).thenReturn(humanDTO);

        // Act
        HumanDTO result = humanService.createHuman(humanDTO);

        // Assert
        assertNotNull(result);
        assertEquals(humanDTO, result);
        verify(entityMapper).toHumanEntity(humanDTO);
        verify(userService).getCurrentUser();
        verify(humanRepository).save(human);
        verify(auditService).auditHuman(human, AuditOperation.CREATE);
        verify(entityMapper).toHumanDTO(human);
    }

    @Test
    @DisplayName("Успешное обновление человека")
    void shouldUpdateHumanSuccessfully() {
        // Arrange
        when(humanRepository.findById(humanDTO.getId())).thenReturn(Optional.of(human));
        when(humanRepository.save(human)).thenReturn(human);
        when(entityMapper.toHumanDTO(human)).thenReturn(humanDTO);

        // Act
        HumanDTO result = humanService.updateHuman(humanDTO);

        // Assert
        assertNotNull(result);
        assertEquals(humanDTO, result);
        verify(humanRepository).findById(humanDTO.getId());
        verify(humanRepository).save(human);
        verify(auditService).auditHuman(human, AuditOperation.UPDATE);
        verify(entityMapper).toHumanDTO(human);
    }

    @Test
    @DisplayName("Ошибка при обновлении несуществующего человека")
    void shouldThrowExceptionWhenUpdatingNonExistingHuman() {
        // Arrange
        when(humanRepository.findById(humanDTO.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> humanService.updateHuman(humanDTO),
                "Ожидалось исключение для отсутствующего человека"
        );

        assertEquals("Human не найден", exception.getMessage());
        verify(humanRepository).findById(humanDTO.getId());
    }

    @Test
    @DisplayName("Успешное удаление человека")
    void shouldDeleteHumanSuccessfully() {
        // Arrange
        Human mockedHuman = mock(Human.class);
        when(mockedHuman.getId()).thenReturn(1L);
        when(mockedHuman.getCities()).thenReturn(Collections.emptyList());
        when(humanRepository.findById(1L)).thenReturn(Optional.of(mockedHuman));

        // Act
        humanService.deleteHuman(1L);

        // Assert
        verify(humanRepository).findById(1L);
        verify(auditService).deleteHumanAuditEntries(1L);
        verify(humanRepository).delete(mockedHuman);
    }

    @Test
    @DisplayName("Ошибка при удалении человека, связанного с городами")
    void shouldThrowExceptionWhenDeletingHumanLinkedToCities() {
        // Arrange
        Human mockedHuman = mock(Human.class); // Создаём мок-объект Human
        when(mockedHuman.getCities()).thenAnswer(invocation -> List.of(new City())); // Используем thenAnswer
        when(humanRepository.findById(1L)).thenReturn(Optional.of(mockedHuman)); // Замокировали метод репозитория

        // Act & Assert
        EntityDeletionException exception = assertThrows(
                EntityDeletionException.class,
                () -> humanService.deleteHuman(1L),
                "Ожидалось исключение при удалении человека, связанного с городами"
        );

        assertEquals(
                "Невозможно удалить Human, поскольку он связан с одним или несколькими Cities",
                exception.getMessage()
        );

        verify(humanRepository).findById(1L);
        verify(mockedHuman, times(2)).getCities(); // Указываем, что метод вызывается дважды
        verifyNoInteractions(auditService);
        verifyNoMoreInteractions(humanRepository);
    }

}
