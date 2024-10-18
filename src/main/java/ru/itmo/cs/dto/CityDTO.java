package ru.itmo.cs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.itmo.cs.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CityDTO {
    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Double area;

    @NotNull
    @Min(1)
    private Long population;

    @NotNull
    private Climate climate;

    @NotNull
    private Government government;

    @NotNull
    private CoordinatesDTO coordinates;

    private Boolean capital;

    private Long metersAboveSeaLevel;

    private StandardOfLiving standardOfLiving;

    private LocalDateTime establishmentDate;

    private HumanDTO governor;

    private User createdBy;

    private LocalDate creationDate;
}


