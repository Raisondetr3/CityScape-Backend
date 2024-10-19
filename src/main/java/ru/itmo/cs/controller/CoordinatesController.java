package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.CoordinatesDTO;
import ru.itmo.cs.dto.PaginationResponseDTO;
import ru.itmo.cs.service.CoordinatesService;

import java.util.List;

@RestController
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {
    private final CoordinatesService coordinatesService;

    @GetMapping
    public ResponseEntity<PaginationResponseDTO<CoordinatesDTO>> getAllCoordinates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "x") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<CoordinatesDTO> coordinatesPage = coordinatesService.getAllCoordinates(page, size, sortBy, sortDir);

        PaginationResponseDTO<CoordinatesDTO> response = new PaginationResponseDTO<>(
                coordinatesPage.getContent(),
                coordinatesPage.getNumber(),
                coordinatesPage.getTotalElements(),
                coordinatesPage.getTotalPages()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<CoordinatesDTO> updateCoordinates(@RequestBody CoordinatesDTO coordinatesDTO) {
        return ResponseEntity.ok(coordinatesService.updateCoordinates( coordinatesDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoordinates(@PathVariable Long id) {
        coordinatesService.deleteCoordinates(id);
        return ResponseEntity.noContent().build();
    }
}