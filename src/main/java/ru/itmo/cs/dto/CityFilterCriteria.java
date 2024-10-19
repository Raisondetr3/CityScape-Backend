package ru.itmo.cs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.enums.Climate;
import ru.itmo.cs.entity.enums.Government;
import ru.itmo.cs.entity.enums.StandardOfLiving;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityFilterCriteria {
    private String name;
    private Climate climate;
    private Government government;
    private StandardOfLiving standardOfLiving;
}
