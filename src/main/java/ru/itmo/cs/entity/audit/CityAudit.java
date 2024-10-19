package ru.itmo.cs.entity.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.audit.AuditOperation;

import java.time.LocalDateTime;

@Entity
@Table(name = "city_audit", schema = "s367911")
@Getter
@Setter
@NoArgsConstructor
public class CityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private AuditOperation operation; // CREATE, UPDATE

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;
}

