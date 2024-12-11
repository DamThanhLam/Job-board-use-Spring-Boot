package vn.edu.iuh.fit.frontend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.iuh.fit.backend.enums.SkillLevel;
import vn.edu.iuh.fit.backend.enums.StatusPostJob;
import vn.edu.iuh.fit.backend.ids.JobSkillId;
import vn.edu.iuh.fit.backend.models.*;
import vn.edu.iuh.fit.backend.services.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;
    private final JobService jobService;
    private final SkillService skillService;
    private final JobSkillService jobSkillService;
    private final CandidateService candidateService;
    private final ExperienceService experienceService;

    public CompanyController(CompanyService companyService, JobService jobService, SkillService skillService, JobSkillService jobSkillService, CandidateService candidateService, ExperienceService experienceService) {
        this.companyService = companyService;
        this.jobService = jobService;
        this.skillService = skillService;
        this.jobSkillService = jobSkillService;
        this.candidateService = candidateService;
        this.experienceService = experienceService;
    }

    @GetMapping
    public String company(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Company> company = companyService.findByEmail(user.getAttribute("email"));
        List<Job> jobs = jobService.findByCompanyId(company.get().getId());
        company.ifPresent(value -> model.addAttribute("company", value));
        jobs.forEach(job -> {
            if (job.getJobSkills() == null) {
                job.setJobSkills(new ArrayList<>());
            }
        });
        model.addAttribute("jobs", jobs);
        return "company/Home";
    }
    @GetMapping
    @RequestMapping("/get-experiences")
    public ResponseEntity<?> getExperiences(@RequestParam("candidateId") Long candidateId){
        List<Experience> experiences = experienceService.findByCandidateId(candidateId);
        System.out.println(experiences.size());
        if(experiences.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(experiences);

    }
    @PostMapping
    @RequestMapping("/add-job")
    @Transactional
    public String addJob(@Valid JobRequest jobRequest, Model model, BindingResult bindingResult) {
        // Kiểm tra dữ liệu nhận được
        System.out.println("Job Name: " + jobRequest.jobName);
        System.out.println("Job Description: " + jobRequest.jobDescription);
        jobRequest.skills.forEach(skill -> {
            System.out.println("Skill ID: " + skill.id);
            System.out.println("Skill Level: " + skill.skillLevel);
            System.out.println("More Infos: " + skill.moreInfos);
        });
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "company/Home";
        }

        // Lấy thông tin công ty dựa trên email người dùng
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Company> companyOptional = companyService.findByEmail(user.getAttribute("email"));

        if (companyOptional.isEmpty()) {
            model.addAttribute("error", "Company not found for the authenticated user.");
            return "company/Home";
        }

        // Tạo Job mới
        Company company = companyOptional.get();
        Job job = new Job();
        job.setCompany(company);
        job.setJobDesc(jobRequest.jobDescription);
        job.setJobName(jobRequest.jobName);
        // Lưu Job vào database
        Job jobResult = jobService.save(job);
        // Kiểm tra và xử lý các SkillRequest
        System.out.println("Skill ID 105: " + jobRequest.skills.size());
        jobRequest.skills.stream()
                .map(skillRequest -> {
                    System.out.println("Skill ID 105: " + skillRequest.id());
                    Optional<Skill> skillOptional = skillService.findById(skillRequest.id());
                    if (skillOptional.isPresent()) {
                        JobSkill jobSkill = new JobSkill();

                        JobSkillId jobSkillId = new JobSkillId();
                        jobSkillId.setJobId(jobResult.getId());
                        jobSkillId.setSkillId(skillOptional.get().getId());
                        jobSkill.setId(jobSkillId);

                        jobSkill.setSkill(skillOptional.get());
                        jobSkill.setSkillLevel(skillRequest.skillLevel());
                        jobSkill.setJob(job);
                        jobSkill.setMoreInfos(skillRequest.moreInfos());
                        return jobSkillService.save(jobSkill);
                    } else {
                        throw new RuntimeException("Skill with ID " + skillRequest.id() + " not found.");
                    }
                }).forEach(savedJobSkill -> System.out.println("Saved JobSkill: " + savedJobSkill));;

        model.addAttribute("message", "Job added successfully!");
        return "company/Home";
    }

    @GetMapping
    @RequestMapping("/find-candidates")
    public String findCandidates(@RequestParam("jobId")long jobId, Model model) {
        List<Candidate> candidates = candidateService.findCandidatesByJobIdAndSkills(jobId);
        model.addAttribute("candidates", candidates);
        return "company/FindCandidates";
    }
    @GetMapping
    @RequestMapping("/status-job")
    public void closeJob(@RequestParam("jobId")long jobId, @RequestParam("status")StatusPostJob status, HttpServletResponse response) throws IOException {
        jobService.close(jobId,status);
        response.sendRedirect("/company");
    }
    private record SkillRequest(
            @NotNull(message = "Skill ID cannot be null.")
            Long id,
            @NotNull(message = "Skill level is required.")
            SkillLevel skillLevel,
            @Size(max = 255,message = "Additional information must not exceed 255 characters.")
            String moreInfos
    ){}
    private record JobRequest(
            @NotBlank(message = "Job name is required.")
            String jobName,
            @Size(max = 500, message = "Job description must not exceed 500 characters.")
            String jobDescription,
            @NotNull(message = "Skill list cannot be null.")
            @Size(min = 1, message = "At least one skill is required.")
            List<SkillRequest> skills
    ) {}

}
