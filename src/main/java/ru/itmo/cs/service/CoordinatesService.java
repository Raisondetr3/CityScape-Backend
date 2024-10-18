package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.CoordinatesDTO;
import ru.itmo.cs.entity.AuditOperation;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.repository.CoordinatesRepository;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;
    private final EntityMapper entityMapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CoordinatesDTO> getAllCoordinates() {
        return coordinatesRepository.findAll()
                .stream()
                .map(entityMapper::toCoordinatesDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CoordinatesDTO getCoordinatesById(Long id) {
        Coordinates coordinates = coordinatesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coordinates not found"));
        return entityMapper.toCoordinatesDTO(coordinates);
    }

    @Transactional
    public CoordinatesDTO createCoordinates(CoordinatesDTO coordinatesDTO) {
        Coordinates coordinates = entityMapper.toCoordinatesEntity(coordinatesDTO);
        Coordinates savedCoordinates = coordinatesRepository.save(coordinates);
        auditService.auditCoordinates(savedCoordinates, AuditOperation.CREATE);
        return entityMapper.toCoordinatesDTO(savedCoordinates);
    }

    @Transactional
    public CoordinatesDTO updateCoordinates(Long id, CoordinatesDTO coordinatesDTO) {
        Coordinates coordinates = coordinatesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coordinates not found"));

        coordinates.setX(coordinatesDTO.getX());
        coordinates.setY(coordinatesDTO.getY());

        Coordinates savedCoordinates = coordinatesRepository.save(coordinates);

        auditService.auditCoordinates(savedCoordinates, AuditOperation.UPDATE);

        return entityMapper.toCoordinatesDTO(savedCoordinates);
    }




    @Transactional
    public void deleteCoordinates(Long id) {
        Coordinates coordinates = coordinatesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coordinates not found"));

        if (!coordinates.getCities().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete coordinates, it's associated with a city");
        }

        auditService.auditCoordinates(coordinates, AuditOperation.DELETE);

        coordinatesRepository.delete(coordinates);
    }
}


