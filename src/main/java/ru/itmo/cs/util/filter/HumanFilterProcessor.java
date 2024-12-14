package ru.itmo.cs.util.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.human.HumanDTO;
import ru.itmo.cs.dto.human.HumanFilterCriteria;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.util.EntityMapper;

@Component
@RequiredArgsConstructor
public class HumanFilterProcessor implements FilterProcessor<HumanDTO, HumanFilterCriteria> {

    private final HumanRepository humanRepository;
    private final EntityMapper entityMapper;

    @Override
    public Page<HumanDTO> filter(HumanFilterCriteria criteria, Pageable pageable) {
        if (criteria.getName() == null || criteria.getName().isEmpty()) {
            return humanRepository.findAll(pageable).map(entityMapper::toHumanDTO);
        } else {
            return humanRepository.findByNameContaining(criteria.getName(), pageable)
                    .map(entityMapper::toHumanDTO);
        }
    }
}
