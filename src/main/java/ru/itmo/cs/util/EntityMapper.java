package ru.itmo.cs.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.dto.CoordinatesDTO;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.service.UserService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EntityMapper {
    public final UserService userService;

    public CityDTO toCityDTO(City city) {
        CityDTO cityDTO = new CityDTO();
        cityDTO.setName(city.getName());
        cityDTO.setArea(city.getArea());
        cityDTO.setPopulation(city.getPopulation());
        cityDTO.setClimate(city.getClimate());
        cityDTO.setGovernment(city.getGovernment());
        cityDTO.setCoordinates(city.getCoordinates());
        cityDTO.setCapital(city.getCapital());
        cityDTO.setMetersAboveSeaLevel(city.getMetersAboveSeaLevel());
        cityDTO.setStandardOfLiving(city.getStandardOfLiving());
        cityDTO.setEstablishmentDate(city.getEstablishmentDate());
        cityDTO.setGovernor(city.getGovernor());
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
                human.getBirthday()
        );
    }

    public CoordinatesDTO toCoordinatesDTO(Coordinates coordinates) {
        return new CoordinatesDTO(
                coordinates.getId(),
                coordinates.getX(),
                coordinates.getY()
        );
    }


    public City toCityEntity(CityDTO cityDTO) {
        City city = new City();
        city.setName(cityDTO.getName());
        city.setArea(cityDTO.getArea());
        city.setPopulation(cityDTO.getPopulation());
        city.setClimate(cityDTO.getClimate());
        city.setGovernment(cityDTO.getGovernment());
        city.setCoordinates(cityDTO.getCoordinates());
        city.setCapital(cityDTO.getCapital());
        city.setMetersAboveSeaLevel(cityDTO.getMetersAboveSeaLevel());
        city.setStandardOfLiving(cityDTO.getStandardOfLiving());
        city.setEstablishmentDate(cityDTO.getEstablishmentDate());
        city.setGovernor(cityDTO.getGovernor());
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
}

