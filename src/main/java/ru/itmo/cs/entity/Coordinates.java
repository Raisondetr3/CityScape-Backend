package ru.itmo.cs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.util.ApplicationContextProvider;

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
    @Max(820)
    private Long x;

    @NotNull
    private Double y;

    @OneToMany(mappedBy = "coordinates", cascade = CascadeType.ALL)
    private List<City> cities;

    @PrePersist
    public void onPrePersist() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCoordinates(this, AuditOperation.CREATE);
    }

    @PreUpdate
    public void onPreUpdate() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCoordinates(this, AuditOperation.UPDATE);
    }

    @PreRemove
    public void onPreRemove() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditCoordinates(this, AuditOperation.DELETE);
    }
}

