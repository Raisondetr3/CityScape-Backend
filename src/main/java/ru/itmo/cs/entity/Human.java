package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.itmo.cs.entity.audit.HumanAudit;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "human")
@Getter
@Setter
@NoArgsConstructor
public class Human {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1)
    private String name;

    @Min(1)
    private int age;

    @Min(1)
    private int height;

    private ZonedDateTime birthday;

    @OneToMany(mappedBy = "governor")
    private List<City> cities;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "human", cascade = CascadeType.REMOVE)
    private List<HumanAudit> audits;
}

