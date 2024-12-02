package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.backend.models.JobSkill;
@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
}
