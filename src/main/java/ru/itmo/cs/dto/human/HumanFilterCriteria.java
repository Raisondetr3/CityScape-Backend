package ru.itmo.cs.dto.human;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HumanFilterCriteria {
    private String name;
}
