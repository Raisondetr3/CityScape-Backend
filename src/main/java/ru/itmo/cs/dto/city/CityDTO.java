package ru.itmo.cs.dto.city;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {

    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Double area;

    @NotNull
    @Min(1)
    private Long population;

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