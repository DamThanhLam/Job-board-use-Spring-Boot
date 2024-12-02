package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.backend.models.Job;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findJobsByCompany_Id(Long company_id);
}
