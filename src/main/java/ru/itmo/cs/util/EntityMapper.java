package ru.itmo.cs.util;

import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.entity.City;

@Component
public class EntityMapper {

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
}

