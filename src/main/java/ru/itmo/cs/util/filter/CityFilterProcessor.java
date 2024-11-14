package ru.itmo.cs.util.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.CityDTO;
import ru.itmo.cs.dto.CityFilterCriteria;
import ru.itmo.cs.repository.CityRepository;
import ru.itmo.cs.util.EntityMapper;


@Component
@RequiredArgsConstructor
public class CityFilterProcessor implements FilterProcessor<CityDTO, CityFilterCriteria> {

    private final CityRepository cityRepository;
    private final EntityMapper entityMapper;

    @Override
    public Page<CityDTO> filter(CityFilterCriteria criteria, Pageable pageable) {
        if ((criteria.getName() == null || criteria.getName().isEmpty()) &&
                (criteria.getGovernorName() == null || criteria.getGovernorName().isEmpty())) {
            return cityRepository.findAll(pageable).map(entityMapper::toCityDTO);
        } else {
            return cityRepository.findByFilters(criteria.getName(), criteria.getGovernorName(), pageable)
                    .map(entityMapper::toCityDTO);
        }
    }
}
