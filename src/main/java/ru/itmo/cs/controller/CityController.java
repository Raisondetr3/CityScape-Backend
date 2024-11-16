package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.dto.PaginationResponseDTO;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;
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
            @RequestParam(required = false) String governorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<CityDTO> citiesPage = cityService.getAllCities(
                name, climate, government, standardOfLiving, governorName, page, size, sortBy, sortDir);

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
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
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

    @DeleteMapping("/government")
    public ResponseEntity<Void> deleteCityByGovernment(@RequestParam Government government) {
        cityService.deleteCityByGovernment(government);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sum-meters-above-sea-level")
    public ResponseEntity<Long> getTotalMetersAboveSeaLevel() {
        Long total = cityService.calculateTotalMetersAboveSeaLevel();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/climate-count")
    public ResponseEntity<Long> countCitiesByClimate(@RequestParam Climate climate) {
        Long count = cityService.countCitiesByClimate(climate);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/route-to-largest-city")
    public ResponseEntity<Double> getRouteToCityWithLargestArea() {
        double distance = cityService.calculateRouteToCityWithLargestArea();
        return ResponseEntity.ok(distance);
    }

    @GetMapping("/route-from-user-to-largest-city")
    public ResponseEntity<Double> getRouteFromUserToLargestCity(
            @RequestParam double userX, @RequestParam double userY, @RequestParam double userZ) {
        double distance = cityService.calculateRouteToCityWithLargestAreaFromUser(userX, userY, userZ);
        return ResponseEntity.ok(distance);
    }
}



