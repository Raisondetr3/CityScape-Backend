package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.entity.AuditOperation;
import ru.itmo.cs.entity.Coordinates;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HumanService {
    private final HumanRepository humanRepository;
    private final AuditService auditService;

    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<HumanDTO> getAllHumans() {
        return humanRepository.findAll()
                .stream()
                .map(entityMapper::toHumanDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HumanDTO getHumanById(Long id) {
        Human human = humanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Human not found"));
        return entityMapper.toHumanDTO(human);
    }

    @Transactional
    public HumanDTO createHuman(HumanDTO humanDTO) {
        Human human = entityMapper.toHumanEntity(humanDTO);
        Human savedHuman = humanRepository.save(human);
        auditService.auditHuman(savedHuman, AuditOperation.CREATE);
        return entityMapper.toHumanDTO(savedHuman);
    }

    @Transactional
    public HumanDTO updateHuman(Long id, HumanDTO humanDTO) {
        Human human = humanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Human not found"));

        human.setName(humanDTO.getName());
        human.setAge(humanDTO.getAge());
        human.setHeight(humanDTO.getHeight());
        human.setBirthday(humanDTO.getBirthday());

        Human savedHuman = humanRepository.save(human);
        auditService.auditHuman(savedHuman, AuditOperation.UPDATE);
        return entityMapper.toHumanDTO(savedHuman);
    }


    @Transactional
    public void deleteHuman(Long id) {
        Human human = humanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Human not found"));

        if (!human.getCities().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete human, it's associated with a city");
        }

        auditService.auditHuman(human, AuditOperation.DELETE);
        humanRepository.delete(human);
    }
}


