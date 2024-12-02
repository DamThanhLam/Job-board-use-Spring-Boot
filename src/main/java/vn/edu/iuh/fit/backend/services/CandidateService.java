package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Candidate;
import vn.edu.iuh.fit.backend.models.Experience;
import vn.edu.iuh.fit.backend.repositories.CandidateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    public void save(Candidate candidate) {
        candidateRepository.save(candidate);
    }

    public Optional<Candidate> findByEmail(String email) {
        return candidateRepository.findByEmail(email);
    }

    public void deleteById(Long id) {
        candidateRepository.deleteById(id);
    }

    public List<Candidate> findAll() {
        return candidateRepository.findAll();
    }
    public Page<Candidate> findAll(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return candidateRepository.findAllByStatus(1, pageable);
    }
    public List<Candidate> findCandidatesByJobIdAndSkills(long jobId) {
        return candidateRepository.findCandidatesByJobIdAndSkills(jobId);
    }
    public Page<Candidate> findCandidatesByJobIdAndSkills(int pageNo, int pageSize, String sortBy, String sortDirection, long jobId) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return candidateRepository.findCandidatesByJobIdAndSkills(jobId,pageable);
    }

}
