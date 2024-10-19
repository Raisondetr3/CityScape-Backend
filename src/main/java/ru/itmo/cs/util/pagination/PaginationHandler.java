package ru.itmo.cs.util.pagination;

import org.springframework.data.domain.Pageable;

public interface PaginationHandler {
    Pageable createPageable(int page, int size, String sortBy, String sortDir);
}

