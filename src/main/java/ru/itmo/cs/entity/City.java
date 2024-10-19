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
import java.util.List;

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
    @Column(name = "name")
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coordinate_id")
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

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Government government;

    @Enumerated(EnumType.STRING)
    private StandardOfLiving standardOfLiving;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "governor_id")
    private Human governor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "city", cascade = CascadeType.REMOVE)
    private List<CityAudit> audits;
}


