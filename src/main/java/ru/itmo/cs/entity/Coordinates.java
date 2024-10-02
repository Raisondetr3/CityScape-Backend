package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
    @Max(820)
    private Long x;

    @NotNull
    private Double y;
}
