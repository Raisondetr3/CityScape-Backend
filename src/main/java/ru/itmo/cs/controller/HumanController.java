package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.service.HumanService;

import java.util.List;

@RestController
@RequestMapping("/api/humans")
@RequiredArgsConstructor
public class HumanController {
    private final HumanService humanService;

    @GetMapping
    public ResponseEntity<List<HumanDTO>> getAllHumans() {
        return ResponseEntity.ok(humanService.getAllHumans());
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
    public ResponseEntity<HumanDTO> updateHuman(@PathVariable Long id, @RequestBody HumanDTO humanDTO) {
        return ResponseEntity.ok(humanService.updateHuman(id, humanDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHuman(@PathVariable Long id) {
        humanService.deleteHuman(id);
        return ResponseEntity.noContent().build();
    }
}


