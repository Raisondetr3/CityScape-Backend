package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.repository.CityAuditRepository;
import ru.itmo.cs.repository.CoordinatesAuditRepository;
import ru.itmo.cs.repository.HumanAuditRepository;
import ru.itmo.cs.util.EntityMapper;


@Service
@RequiredArgsConstructor
public class AuditService {

    private final CityAuditRepository cityAuditRepository;
    private final HumanAuditRepository humanAuditRepository;
    private final CoordinatesAuditRepository coordinatesAuditRepository;
    private final EntityMapper entityMapper;

    public void auditCity(City city, AuditOperation operation) {
        CityAudit cityAudit = entityMapper.toCityAudit(city, operation);
        cityAuditRepository.save(cityAudit);
    }

    public void auditHuman(Human human, AuditOperation operation) {
        HumanAudit humanAudit = entityMapper.toHumanAudit(human, operation);
        humanAuditRepository.save(humanAudit);
    }

    public void auditCoordinates(Coordinates coordinates, AuditOperation operation) {
        CoordinatesAudit coordinatesAudit = entityMapper.toCoordinatesAudit(coordinates, operation);
        coordinatesAuditRepository.save(coordinatesAudit);
    }
}

