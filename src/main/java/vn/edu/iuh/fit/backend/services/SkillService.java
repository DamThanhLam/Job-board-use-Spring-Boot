package vn.edu.iuh.fit.backend.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Skill;
import vn.edu.iuh.fit.backend.repositories.SkillRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {
    @Autowired
    private SkillRepository skillRepository;
    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    public List<Skill> findByName(String name) {
        return skillRepository.findBySkillName(name);
    }

    public Optional<Skill> findById(long id) {
        return skillRepository.findById(id);
    }
}
