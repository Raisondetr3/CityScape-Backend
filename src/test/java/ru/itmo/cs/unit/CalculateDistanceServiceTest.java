package ru.itmo.cs.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.cs.service.CalculateDistanceService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalculateDistanceServiceTest {

    @InjectMocks
    private CalculateDistanceService calculateDistanceService;

    @Test
    @DisplayName("Успешный расчет расстояния между двумя точками")
    void shouldCalculateDistanceSuccessfully() {
        // Arrange
        double x1 = 1.0, y1 = 2.0, z1 = 3.0;
        double x2 = 4.0, y2 = 6.0, z2 = 8.0;
        double expectedDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));

        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(expectedDistance, result, 0.0001, "Расстояние рассчитано неверно");
    }

    @Test
    @DisplayName("Расчет расстояния между двумя точками с одинаковыми координатами")
    void shouldReturnZeroForIdenticalPoints() {
        // Arrange
        double x1 = 1.0, y1 = 2.0, z1 = 3.0;
        double x2 = 1.0, y2 = 2.0, z2 = 3.0;

        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(0.0, result, 0.0001, "Расстояние между одинаковыми точками должно быть 0");
    }

    @Test
    @DisplayName("Расчет расстояния с отрицательными координатами")
    void shouldCalculateDistanceWithNegativeCoordinates() {
        // Arrange
        double x1 = -1.0, y1 = -2.0, z1 = -3.0;
        double x2 = -4.0, y2 = -6.0, z2 = -8.0;
        double expectedDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));

        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(expectedDistance, result, 0.0001, "Расстояние рассчитано неверно для отрицательных координат");
    }

    @Test
    @DisplayName("Расчет расстояния с координатами, содержащими нули")
    void shouldCalculateDistanceWithZeroCoordinates() {
        // Arrange
        double x1 = 0.0, y1 = 0.0, z1 = 0.0;
        double x2 = 3.0, y2 = 4.0, z2 = 0.0; // Расстояние = 5 (по теореме Пифагора)
        double expectedDistance = 5.0;

        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(expectedDistance, result, 0.0001, "Расстояние рассчитано неверно для координат с нулями");
    }

    @Test
    @DisplayName("Расчет расстояния для больших координат")
    void shouldCalculateDistanceWithLargeCoordinates() {
        // Arrange
        double x1 = 1e6, y1 = 2e6, z1 = 3e6;
        double x2 = 4e6, y2 = 6e6, z2 = 8e6;
        double expectedDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));

        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(expectedDistance, result, 0.0001, "Расстояние рассчитано неверно для больших координат");
    }

    @Test
    @DisplayName("Ошибка при NaN-координатах")
    void shouldThrowExceptionForNaNCoordinates() {
        // Arrange
        double x1 = Double.NaN, y1 = 2.0, z1 = 3.0;
        double x2 = 4.0, y2 = 6.0, z2 = 8.0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2),
                "Ожидалось исключение для NaN координат"
        );

        assertEquals("Координаты не должны содержать NaN", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0, 3, 4, 0, 5.0",         // Простая точка с нулями (расстояние = 5)
            "1, 2, 3, 4, 6, 8, 7.0711",      // Обычный случай
            "-1, -2, -3, -4, -6, -8, 7.0711", // Отрицательные координаты
            "1e6, 2e6, 3e6, 4e6, 6e6, 8e6, 7071067.8119", // Большие координаты
            "1, 1, 1, 1, 1, 1, 0.0"          // Одинаковые точки (расстояние = 0)
    })
    @DisplayName("Параметризованный тест: расчет расстояния между двумя точками")
    void shouldCalculateDistanceParameterized(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double expectedDistance
    ) {
        // Act
        double result = calculateDistanceService.calculate(x1, y1, z1, x2, y2, z2);

        // Assert
        assertEquals(expectedDistance, result, 0.0001, "Расстояние рассчитано неверно");
    }
}
