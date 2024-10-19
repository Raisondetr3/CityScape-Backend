package ru.itmo.cs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginationResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}
