package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.backend.enums.StatusPostJob;
import vn.edu.iuh.fit.backend.models.Job;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findJobsByCompany_Id(Long company_id);

    @Transactional
    @Modifying
    @Query("update Job j set j.status = ?1 where j.id = ?2")
    void updateStatusById(StatusPostJob status, Long id);

    @Query("SELECT DISTINCT j FROM Candidate c JOIN c.candidateSkills cs JOIN cs.skill s JOIN s.jobSkills js JOIN js.job j WHERE c.id = ?1 AND j.status != 0")
    List<Job> suggestJobByCandidateId(Long candidateId);
}
