package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.dto.PaginationResponseDTO;
import ru.itmo.cs.entity.Climate;
import ru.itmo.cs.entity.Government;
import ru.itmo.cs.entity.StandardOfLiving;
import ru.itmo.cs.service.CityService;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<PaginationResponseDTO<CityDTO>> getAllCities(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Climate climate,
            @RequestParam(required = false) Government government,
            @RequestParam(required = false) StandardOfLiving standardOfLiving,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {


        Page<CityDTO> citiesPage = cityService.getAllCities(name,
                climate,
                government,
                standardOfLiving,
                page,
                size,
                sortBy,
                sortDir);

        PaginationResponseDTO<CityDTO> response = new PaginationResponseDTO<>(
                citiesPage.getContent(),
                citiesPage.getNumber(),
                citiesPage.getTotalElements(),
                citiesPage.getTotalPages()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping("/{id}")
    public ResponseEntity<CityDTO> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @PostMapping
    public ResponseEntity<CityDTO> createCity(@RequestBody CityDTO cityDTO) {
        CityDTO createdCity = cityService.createCity(cityDTO);
        return ResponseEntity.ok(createdCity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityDTO> updateCity(@PathVariable Long id, @RequestBody CityDTO cityDTO) {
        CityDTO updatedCity = cityService.updateCity(id, cityDTO);
        return ResponseEntity.ok(updatedCity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}

