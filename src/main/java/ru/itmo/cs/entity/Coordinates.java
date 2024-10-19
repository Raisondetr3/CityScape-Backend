package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "coordinates", schema = "s367911")
@Getter
@Setter
@NoArgsConstructor
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Max(820)
    private Long x;

    @NotNull
    private Double y;

    @OneToMany(mappedBy = "coordinates")
    private List<City> cities;

    @OneToMany(mappedBy = "coordinates", cascade = CascadeType.REMOVE)
    private List<CoordinatesAudit> audits;
}

