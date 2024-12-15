package ru.itmo.cs.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.city.CityDTO;
import ru.itmo.cs.dto.coordinates.CoordinatesDTO;
import ru.itmo.cs.dto.ImportOperationDTO;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.dto.auth.UserDTO;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.entity.audit.AuditOperation;
import ru.itmo.cs.entity.audit.CityAudit;
import ru.itmo.cs.entity.audit.CoordinatesAudit;
import ru.itmo.cs.entity.audit.HumanAudit;
import ru.itmo.cs.entity.enums.ImportStatus;
import ru.itmo.cs.service.CoordinatesService;
import ru.itmo.cs.service.HumanService;
import ru.itmo.cs.service.UserService;

import java.time.LocalDateTime;

@Component
public class EntityMapper {

    public UserService userService;
    public HumanService humanService;
    public CoordinatesService coordinatesService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setHumanService(HumanService humanService) {
        this.humanService = humanService;
    }

    @Autowired
    public void setCoordinatesService(CoordinatesService coordinatesService) {
        this.coordinatesService = coordinatesService;
    }

    public CityDTO toCityDTO(City city) {
        CityDTO cityDTO = new CityDTO();
        cityDTO.setId(city.getId());
        cityDTO.setName(city.getName());
        cityDTO.setArea(city.getArea());
        cityDTO.setPopulation(city.getPopulation());
        cityDTO.setClimate(city.getClimate());
        cityDTO.setGovernment(city.getGovernment());
        cityDTO.setCoordinates(toCoordinatesDTO(city.getCoordinates()));
        cityDTO.setCapital(city.getCapital());
        cityDTO.setMetersAboveSeaLevel(city.getMetersAboveSeaLevel());
        cityDTO.setStandardOfLiving(city.getStandardOfLiving());
        cityDTO.setEstablishmentDate(city.getEstablishmentDate());
        cityDTO.setGovernor(toHumanDTO(city.getGovernor()));
        cityDTO.setCreatedBy(city.getCreatedBy());
        cityDTO.setCreationDate(city.getCreationDate());
        return cityDTO;
    }

    public HumanDTO toHumanDTO(Human human) {
        return new HumanDTO(
                human.getId(),
                human.getName(),
                human.getAge(),
                human.getHeight(),
                human.getBirthday(),
                human.getCreatedBy()
        );
    }

    public CoordinatesDTO toCoordinatesDTO(Coordinates coordinates) {
        return new CoordinatesDTO(
                coordinates.getId(),
                coordinates.getX(),
                coordinates.getY(),
                coordinates.getCreatedBy()
        );
    }

    public UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setAdminRequestStatus(user.getAdminRequestStatus());
        return userDTO;
    }

    public ImportOperationDTO toImportOperationDTO(ImportOperation operation) {
        ImportOperationDTO dto = new ImportOperationDTO();
        dto.setId(operation.getId());
        dto.setStatus(operation.getStatus());
        dto.setTimestamp(operation.getTimestamp());
        dto.setObjectsAdded(operation.getObjectsAdded());
        dto.setUsername(operation.getUser().getUsername());
        return dto;
    }


    public City toCityEntity(CityDTO cityDTO, Coordinates coordinates, Human governor) {
        City city = new City();
        city.setName(cityDTO.getName());
        city.setArea(cityDTO.getArea());
        city.setPopulation(cityDTO.getPopulation());
        city.setClimate(cityDTO.getClimate());
        city.setGovernment(cityDTO.getGovernment());
        city.setCoordinates(coordinates);
        city.setCapital(cityDTO.getCapital());
        city.setMetersAboveSeaLevel(cityDTO.getMetersAboveSeaLevel());
        city.setStandardOfLiving(cityDTO.getStandardOfLiving());
        city.setEstablishmentDate(cityDTO.getEstablishmentDate());
        city.setGovernor(governor);
        // `createdBy` и `creationDate` не меняются при создании/обновлении
        return city;
    }

    public Human toHumanEntity(HumanDTO humanDTO) {
        Human human = new Human();
        human.setName(humanDTO.getName());
        human.setAge(humanDTO.getAge());
        human.setHeight(humanDTO.getHeight());
        human.setBirthday(humanDTO.getBirthday());
        return human;
    }

    public Coordinates toCoordinatesEntity(CoordinatesDTO coordinatesDTO) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(coordinatesDTO.getX());
        coordinates.setY(coordinatesDTO.getY());
        return coordinates;
    }


    public CityAudit toCityAudit(City city, AuditOperation operation) {
        CityAudit cityAudit = new CityAudit();
        cityAudit.setCity(city);
        cityAudit.setUser(userService.getCurrentUser());
        cityAudit.setOperation(operation);
        cityAudit.setOperationTime(LocalDateTime.now());
        return cityAudit;
    }

    public HumanAudit toHumanAudit(Human human, AuditOperation operation) {
        HumanAudit humanAudit = new HumanAudit();
        humanAudit.setHuman(human);
        humanAudit.setUser(userService.getCurrentUser());
        humanAudit.setOperation(operation);
        humanAudit.setOperationTime(LocalDateTime.now());
        return humanAudit;
    }

    public CoordinatesAudit toCoordinatesAudit(Coordinates coordinates, AuditOperation operation) {
        CoordinatesAudit coordinatesAudit = new CoordinatesAudit();
        coordinatesAudit.setCoordinates(coordinates);
        coordinatesAudit.setUser(userService.getCurrentUser());
        coordinatesAudit.setOperation(operation);
        coordinatesAudit.setOperationTime(LocalDateTime.now());
        return coordinatesAudit;
    }

    public ImportOperation toImportOperationEntity(User user, ImportStatus status, int objectsAdded) {
        ImportOperation operation = new ImportOperation();
        operation.setUser(user);
        operation.setStatus(status);
        operation.setTimestamp(LocalDateTime.now());
        operation.setObjectsAdded(objectsAdded);
        return operation;
    }
}