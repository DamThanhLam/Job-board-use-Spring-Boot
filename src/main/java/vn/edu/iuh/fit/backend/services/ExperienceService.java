package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Experience;
import vn.edu.iuh.fit.backend.repositories.ExperienceRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExperienceService {
    @Autowired
    private ExperienceRepository experienceRepository;
    public Experience save(Experience experience) {
        return experienceRepository.save(experience);
    }

    public List<Experience> findByCanId(Long id) {
        return experienceRepository.findByCandidate_Id(id);
    }
    public boolean isExperienceOverlap(LocalDate from, LocalDate to) {
        if(experienceRepository.findExperienceOverlap(from, to).isPresent()){
            return true;
        }
        return false;
    }
    public List<Experience> findByCandidateId(Long id) {
        return experienceRepository.findByCandidate_Id(id);
    }
}
