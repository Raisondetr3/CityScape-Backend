package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.cs.entity.enums.ImportStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_operations")
@Getter
@Setter
@NoArgsConstructor
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ImportStatus status;

    private LocalDateTime timestamp;

    private int objectsAdded;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String fileName;
}