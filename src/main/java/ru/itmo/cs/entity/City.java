package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.util.ApplicationContextProvider;

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

    @ManyToOne
    @JoinColumn(name = "coordinate_id", nullable = false)
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
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "governor_id")
    private Human governor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PrePersist
    public void onPrePersist() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCity(this, AuditOperation.CREATE);
    }

    @PreUpdate
    public void onPreUpdate() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCity(this, AuditOperation.UPDATE);
    }

    @PreRemove
    public void onPreRemove() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCity(this, AuditOperation.DELETE);
    }
}

