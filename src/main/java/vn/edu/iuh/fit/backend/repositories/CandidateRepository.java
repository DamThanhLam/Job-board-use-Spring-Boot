package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.backend.models.Candidate;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);

    Page<Candidate> findAllByStatus(int i, Pageable pageable);

    @Query("SELECT c FROM Candidate c " +
            "JOIN c.candidateSkills cs " +
            "JOIN cs.skill s " +
            "JOIN JobSkill js ON js.skill = s " +
            "JOIN js.job j " +
            "WHERE j.id = :jobId")
    List<Candidate> findCandidatesByJobIdAndSkills(@Param("jobId") Long jobId);

    @Query("SELECT c FROM Candidate c " +
            "JOIN c.candidateSkills cs " +
            "JOIN cs.skill s " +
            "JOIN JobSkill js ON js.skill = s " +
            "JOIN js.job j " +
            "WHERE j.id = :jobId")
    Page<Candidate> findCandidatesByJobIdAndSkills(@Param("jobId") Long jobId, Pageable pageable);


}
