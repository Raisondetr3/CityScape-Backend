package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.entity.audit.CoordinatesAudit;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coordinates")
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
    private List<City> cities  = new ArrayList<>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "coordinates")
    private List<CoordinatesAudit> audits;
}