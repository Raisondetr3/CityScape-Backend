package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.itmo.cs.service.AuditService;
import ru.itmo.cs.util.ApplicationContextProvider;

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

    @OneToMany(mappedBy = "coordinates", cascade = CascadeType.ALL)
    private List<City> cities;

    @PrePersist
    public void onPrePersist() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditHuman(this, AuditOperation.CREATE);
    }

    @PreUpdate
    public void onPreUpdate() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditHuman(this, AuditOperation.UPDATE);
    }

    @PreRemove
    public void onPreRemove() {
        AuditService auditService = ApplicationContextProvider.getBean(AuditService.class);
        auditService.auditHuman(this, AuditOperation.DELETE);
    }
}

