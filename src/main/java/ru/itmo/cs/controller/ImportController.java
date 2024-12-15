package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.cs.dto.ImportOperationDTO;
import ru.itmo.cs.exception.FileReadException;
import ru.itmo.cs.service.ImportService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<ImportOperationDTO> importCities(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileReadException("Ошибка чтения файла: файл пустой");
            }
            String jsonData = new String(file.getBytes(), StandardCharsets.UTF_8);
            ImportOperationDTO importOperationDTO = importService.importCities(jsonData);
            return ResponseEntity.ok(importOperationDTO);
        } catch (IOException e) {
            throw new FileReadException("Ошибка чтения файла: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportOperationDTO>> getImportHistory() {
        List<ImportOperationDTO> history = importService.getImportHistory();
        return ResponseEntity.ok(history);
    }
}
