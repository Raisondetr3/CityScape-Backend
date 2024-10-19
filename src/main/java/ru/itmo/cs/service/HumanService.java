package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.HumanDTO;
import ru.itmo.cs.dto.HumanFilterCriteria;
import ru.itmo.cs.entity.AuditOperation;
import ru.itmo.cs.entity.Human;
import ru.itmo.cs.exception.EntityDeletionException;
import ru.itmo.cs.repository.HumanRepository;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HumanService {
    private final HumanRepository humanRepository;
    private final AuditService auditService;
    private final EntityMapper entityMapper;
    private final FilterProcessor<HumanDTO, HumanFilterCriteria> humanFilterProcessor;
    private final PaginationHandler paginationHandler;

    @Transactional(readOnly = true)
    public Page<HumanDTO> getAllHumans(String name, int page, int size, String sortBy, String sortDir) {
        HumanFilterCriteria criteria = new HumanFilterCriteria();
        criteria.setName(name);

        Pageable pageable = paginationHandler.createPageable(page, size, sortBy, sortDir);
        return humanFilterProcessor.filter(criteria, pageable);
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
    public HumanDTO updateHuman(HumanDTO humanDTO) {
        Human human = humanRepository.findById(humanDTO.getId())
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
    public Human createOrUpdateHumanForCity(HumanDTO humanDTO) {
        // Determining whether to create a new object or update an existing one
        if (humanDTO.getId() != null) {
            Human existingHuman = humanRepository.findById(humanDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Human not found"));

            existingHuman.setName(humanDTO.getName());
            existingHuman.setAge(humanDTO.getAge());
            existingHuman.setHeight(humanDTO.getHeight());
            existingHuman.setBirthday(humanDTO.getBirthday());

            Human savedHuman = humanRepository.save(existingHuman);
            auditService.auditHuman(savedHuman, AuditOperation.UPDATE);
            return savedHuman;
        } else {
            Human human = entityMapper.toHumanEntity(humanDTO);
            Human savedHuman = humanRepository.save(human);
            auditService.auditHuman(savedHuman, AuditOperation.CREATE);
            return savedHuman;
        }
    }

    @Transactional
    public void deleteHuman(Long id) {
        Human human = humanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Human not found"));

        if (!human.getCities().isEmpty()) {
            throw new EntityDeletionException("Cannot delete Human as it is associated with one or more Cities.");
        }

        humanRepository.delete(human);
    }
}

