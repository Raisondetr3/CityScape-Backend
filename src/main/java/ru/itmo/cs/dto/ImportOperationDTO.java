package ru.itmo.cs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.enums.ImportStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportOperationDTO {
    private Long id;
    private ImportStatus status;
    private LocalDateTime timestamp;
    private int objectsAdded;
    private String username;
    private String fileDownloadUrl;
}

