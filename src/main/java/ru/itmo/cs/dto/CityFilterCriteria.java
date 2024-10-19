package ru.itmo.cs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.itmo.cs.entity.Climate;
import ru.itmo.cs.entity.Government;
import ru.itmo.cs.entity.StandardOfLiving;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityFilterCriteria {
    private String name;
    private Climate climate;
    private Government government;
    private StandardOfLiving standardOfLiving;
}
