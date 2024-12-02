package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.CandidateSkill;
import vn.edu.iuh.fit.backend.repositories.CandidateSkillRepository;

import java.util.List;


@Service
public class CandidateSkillService {
    @Autowired
    private CandidateSkillRepository candidateSkillRepository;
    public CandidateSkill save(CandidateSkill candidateSkill) {
        return candidateSkillRepository.save(candidateSkill);
    }

    public List<CandidateSkill> findByCanId(Long id) {
        return candidateSkillRepository.findByCan_Id(id);
    }
}
