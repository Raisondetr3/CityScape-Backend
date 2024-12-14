package ru.itmo.cs.entity.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.audit.AuditOperation;

import java.time.LocalDateTime;

@Entity
@Table(name = "coordinates_audit")
@Getter
@Setter
@NoArgsConstructor
public class CoordinatesAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private AuditOperation operation;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;
}

