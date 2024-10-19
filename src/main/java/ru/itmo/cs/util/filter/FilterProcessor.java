package ru.itmo.cs.util.filter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.dto.HumanFilterCriteria;

public interface FilterProcessor<T, F> {
    Page<T> filter(F filterCriteria, Pageable pageable);
}

