package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @Column(name = "name")
    private String name;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "coordinate_id", referencedColumnName = "id")
    private Coordinates coordinates;

    @Column(nullable = false)
    private LocalDate creationDate = LocalDate.now();

    @NotNull
    @Column(name = "area")
    @Min(1)
    private double area;

    @Column(nullable = false)
    @Min(1)
    private Long population;

    private LocalDateTime establishmentDate;

    @Column(nullable = false)
    private Boolean capital;

    private long metersAboveSeaLevel;

    @Enumerated(EnumType.STRING)
    private Climate climate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Government government;

    @Enumerated(EnumType.STRING)
    private StandardOfLiving standardOfLiving;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "governor_id", referencedColumnName = "id")
    private Human governor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;
}

