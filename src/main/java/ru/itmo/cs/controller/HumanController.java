package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.dto.PaginationResponseDTO;
import ru.itmo.cs.service.HumanService;

import java.util.List;

@RestController
@RequestMapping("/api/humans")
@RequiredArgsConstructor
public class HumanController {
    private final HumanService humanService;

    @GetMapping
    public ResponseEntity<PaginationResponseDTO<HumanDTO>> getAllHumans(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<HumanDTO> humansPage = humanService.getAllHumans(name, page, size, sortBy, sortDir);

        PaginationResponseDTO<HumanDTO> response = new PaginationResponseDTO<>(
                humansPage.getContent(),
                humansPage.getNumber(),
                humansPage.getTotalElements(),
                humansPage.getTotalPages()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HumanDTO> getHumanById(@PathVariable Long id) {
        return ResponseEntity.ok(humanService.getHumanById(id));
    }

    @PostMapping
    public ResponseEntity<HumanDTO> createHuman(@RequestBody HumanDTO humanDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(humanService.createHuman(humanDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HumanDTO> updateHuman(@RequestBody HumanDTO humanDTO) {
        return ResponseEntity.ok(humanService.updateHuman(humanDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHuman(@PathVariable Long id) {
        humanService.deleteHuman(id);
        return ResponseEntity.noContent().build();
    }
}


