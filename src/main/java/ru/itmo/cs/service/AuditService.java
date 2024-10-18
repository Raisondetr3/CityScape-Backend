package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.repository.CityAuditRepository;
import ru.itmo.cs.repository.CoordinatesAuditRepository;
import ru.itmo.cs.repository.HumanAuditRepository;
import ru.itmo.cs.util.EntityMapper;


@Service
public class AuditService {

    private CityAuditRepository cityAuditRepository;
    private HumanAuditRepository humanAuditRepository;
    private CoordinatesAuditRepository coordinatesAuditRepository;
    private EntityMapper entityMapper;

    @Autowired
    public void setCoordinatesAuditRepository(CoordinatesAuditRepository coordinatesAuditRepository) {
        this.coordinatesAuditRepository = coordinatesAuditRepository;
    }

    @Autowired
    public void setCityAuditRepository(CityAuditRepository cityAuditRepository) {
        this.cityAuditRepository = cityAuditRepository;
    }

    @Autowired
    public void setHumanAuditRepository(HumanAuditRepository humanAuditRepository) {
        this.humanAuditRepository = humanAuditRepository;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @Transactional
    public void auditCity(City city, AuditOperation operation) {
        CityAudit cityAudit = entityMapper.toCityAudit(city, operation);
        cityAuditRepository.save(cityAudit);
    }

    @Transactional
    public void auditHuman(Human human, AuditOperation operation) {
        HumanAudit humanAudit = entityMapper.toHumanAudit(human, operation);
        humanAuditRepository.save(humanAudit);
    }

    @Transactional
    public void auditCoordinates(Coordinates coordinates, AuditOperation operation) {
        CoordinatesAudit coordinatesAudit = entityMapper.toCoordinatesAudit(coordinates, operation);
        coordinatesAuditRepository.save(coordinatesAudit);
    }
}

