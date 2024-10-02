package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "city", schema = "s367911")
@Getter
@Setter
@NoArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "coordinate_id", referencedColumnName = "id")
    @NotNull
    private Coordinates coordinates;

    @NotNull
    private LocalDate creationDate = LocalDate.now();

    @Min(1)
    private double area;

    @NotNull
    @Min(1)
    private Long population;

    private LocalDateTime establishmentDate;

    @NotNull
    private Boolean capital;

    private long metersAboveSeaLevel;

    @Enumerated(EnumType.STRING)
    private Climate climate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Government government;

    @Enumerated(EnumType.STRING)
    private StandardOfLiving standardOfLiving;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "governor_id", referencedColumnName = "id")
    @NotNull
    private Human governor;
}

