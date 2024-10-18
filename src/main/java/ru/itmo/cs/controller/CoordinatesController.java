package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.CoordinatesDTO;
import ru.itmo.cs.service.CoordinatesService;

import java.util.List;

@RestController
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {
    private final CoordinatesService coordinatesService;

    @GetMapping
    public ResponseEntity<List<CoordinatesDTO>> getAllCoordinates() {
        return ResponseEntity.ok(coordinatesService.getAllCoordinates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> getCoordinatesById(@PathVariable Long id) {
        return ResponseEntity.ok(coordinatesService.getCoordinatesById(id));
    }

    @PostMapping
    public ResponseEntity<CoordinatesDTO> createCoordinates(@RequestBody CoordinatesDTO coordinatesDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coordinatesService.createCoordinates(coordinatesDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> updateCoordinates(@PathVariable Long id, @RequestBody CoordinatesDTO coordinatesDTO) {
        return ResponseEntity.ok(coordinatesService.updateCoordinates(id, coordinatesDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoordinates(@PathVariable Long id) {
        coordinatesService.deleteCoordinates(id);
        return ResponseEntity.noContent().build();
    }
}
