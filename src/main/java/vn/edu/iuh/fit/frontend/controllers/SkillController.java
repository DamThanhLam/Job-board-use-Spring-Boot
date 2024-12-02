package vn.edu.iuh.fit.frontend.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.iuh.fit.backend.models.Candidate;
import vn.edu.iuh.fit.backend.models.Company;
import vn.edu.iuh.fit.backend.models.Job;
import vn.edu.iuh.fit.backend.models.Skill;
import vn.edu.iuh.fit.backend.services.CandidateService;
import vn.edu.iuh.fit.backend.services.CompanyService;
import vn.edu.iuh.fit.backend.services.JobService;
import vn.edu.iuh.fit.backend.services.SkillService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/skills")
public class SkillController {
    private final SkillService skillService;
    private final CandidateService candidateService;
    private final CompanyService companyService;
    private final JobService jobService;

    public SkillController(SkillService skillService, CandidateService candidateService, CompanyService companyService, JobService jobService) {
        this.skillService = skillService;
        this.candidateService = candidateService;
        this.companyService = companyService;
        this.jobService = jobService;
    }

    @PostMapping
    @RequestMapping("/add")
    public String addSkill(Skill skill, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));
        Optional<Company> company = companyService.findByEmail(user.getAttribute("email"));
        if(candidate.isPresent() || company.isPresent()) {
            if(skill != null) {
                if(!skill.getSkillDescription().trim().isEmpty()){
                    if(!skill.getSkillName().trim().isEmpty()){
                        System.out.println(skill.getType());
                        if(skill.getType() != null){
                            skill.setId(null);
                            skillService.save(skill);
                            model.addAttribute("message", "Skill added successfully");
                        }else{
                            model.addAttribute("error", "Skill type cannot be empty");
                        }
                    }else{
                        model.addAttribute("error", "Skill name cannot be empty");
                    }
                }else{
                    model.addAttribute("error", "Skill description cannot be empty");
                }
            }else{
                model.addAttribute("error", "Skill null");
            }
            if(candidate.isPresent()) {
                return "candidates/Home";
            }else{
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
        }
        return "redirect:/";
    }
}
