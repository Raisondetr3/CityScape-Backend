package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.itmo.cs.entity.audit.HumanAudit;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "human")
@Getter
@Setter
@NoArgsConstructor
public class Human {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1)
    private String name;

    @Min(1)
    private int age;

    @Min(1)
    private int height;

    private ZonedDateTime birthday;

    @OneToMany(mappedBy = "governor")
    private List<City> cities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "human")
    private List<HumanAudit> audits;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Human human = (Human) o;
        return age == human.age &&
                height == human.height &&
                Objects.equals(name, human.name) &&
                Objects.equals(birthday, human.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, height, birthday);
    }
}