package ru.itmo.cs.util.filter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FilterProcessor<T, F> {
    Page<T> filter(F filterCriteria, Pageable pageable);
}
