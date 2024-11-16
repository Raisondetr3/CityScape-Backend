package ru.itmo.cs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.CoordinatesDTO;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.exception.EntityDeletionException;
import ru.itmo.cs.repository.CoordinatesRepository;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.pagination.PaginationHandler;

@Service
public class CoordinatesService {
    private CoordinatesRepository coordinatesRepository;
    private EntityMapper entityMapper;
    private AuditService auditService;
    private PaginationHandler paginationHandler;

    @Autowired
    public void setCoordinatesRepository(CoordinatesRepository coordinatesRepository) {
        this.coordinatesRepository = coordinatesRepository;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @Autowired
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Autowired
    public void setPaginationHandler(PaginationHandler paginationHandler) {
        this.paginationHandler = paginationHandler;
    }

    @Transactional(readOnly = true)
    public Page<CoordinatesDTO> getAllCoordinates(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = paginationHandler.createPageable(page, size, sortBy, sortDir);
        return coordinatesRepository.findAll(pageable).map(entityMapper::toCoordinatesDTO);
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
    public CoordinatesDTO updateCoordinates( CoordinatesDTO coordinatesDTO) {
        Coordinates coordinates = coordinatesRepository.findById(coordinatesDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Coordinates not found"));

        coordinates.setX(coordinatesDTO.getX());
        coordinates.setY(coordinatesDTO.getY());

        Coordinates savedCoordinates = coordinatesRepository.save(coordinates);

        auditService.auditCoordinates(savedCoordinates, AuditOperation.UPDATE);

        return entityMapper.toCoordinatesDTO(savedCoordinates);
    }

    @Transactional
    public Coordinates createOrUpdateCoordinatesForCity(CoordinatesDTO coordinatesDTO) {
        // Determining whether to create a new object or update an existing one
        if (coordinatesDTO.getId() != null) {
            Coordinates existingCoordinates = coordinatesRepository.findById(coordinatesDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Coordinates not found"));

            existingCoordinates.setX(coordinatesDTO.getX());
            existingCoordinates.setY(coordinatesDTO.getY());

            Coordinates savedCoordinates = coordinatesRepository.save(existingCoordinates);
            auditService.auditCoordinates(savedCoordinates, AuditOperation.UPDATE);
            return savedCoordinates;
        } else {
            Coordinates coordinates = entityMapper.toCoordinatesEntity(coordinatesDTO);
            Coordinates savedCoordinates = coordinatesRepository.save(coordinates);
            auditService.auditCoordinates(savedCoordinates, AuditOperation.CREATE);
            return savedCoordinates;
        }
    }


    @Transactional
    public void deleteCoordinates(Long id) {
        Coordinates coordinates = coordinatesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coordinates не найдены"));

        if (!coordinates.getCities().isEmpty()) {
            throw new EntityDeletionException("Невозможно удалить Coordinates," +
                    " поскольку они связаны с одним или несколькими Cities");
        }

        auditService.deleteCoordinatesAuditEntries(coordinates.getId());

        coordinatesRepository.delete(coordinates);
    }
}


