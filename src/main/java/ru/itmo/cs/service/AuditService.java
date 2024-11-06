package ru.itmo.cs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.audit.CityAudit;
import ru.itmo.cs.entity.audit.CoordinatesAudit;
import ru.itmo.cs.entity.audit.HumanAudit;
import ru.itmo.cs.repository.audit.CityAuditRepository;
import ru.itmo.cs.repository.audit.CoordinatesAuditRepository;
import ru.itmo.cs.repository.audit.HumanAuditRepository;
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

    @Transactional
    public void deleteCityAuditEntries(Long cityId) {
        cityAuditRepository.deleteAllByCityId(cityId);
    }

    @Transactional
    public void deleteCoordinatesAuditEntries(Long coordinatesId) {
        coordinatesAuditRepository.deleteAllByCoordinatesId(coordinatesId);
    }

    @Transactional
    public void deleteHumanAuditEntries(Long humanId) {
        humanAuditRepository.deleteAllByHumanId(humanId);
    }
}

