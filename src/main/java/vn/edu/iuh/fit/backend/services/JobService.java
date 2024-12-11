package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.enums.StatusPostJob;
import vn.edu.iuh.fit.backend.models.Job;
import vn.edu.iuh.fit.backend.repositories.JobRepository;

import java.util.List;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;
    public List<Job> findByCompanyId(long companyId) {
        return  jobRepository.findJobsByCompany_Id(companyId);
    }
    public Job save(Job job) {
        return jobRepository.save(job);
    }

    public void close(long jobId, StatusPostJob status) {
        jobRepository.updateStatusById(status,jobId);
    }
}
