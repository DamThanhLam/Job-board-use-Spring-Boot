package vn.edu.iuh.fit.frontend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.iuh.fit.backend.enums.SkillLevel;
import vn.edu.iuh.fit.backend.enums.SkillType;
import vn.edu.iuh.fit.backend.ids.CandidateSkillId;
import vn.edu.iuh.fit.backend.models.*;
import vn.edu.iuh.fit.backend.services.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/candidate")
public class CandidateController {
    private final CandidateService candidateService;
    private final SkillService skillService;
    private final CandidateSkillService candidateSkillService;
    private final ExperienceService experienceService;
    private final JobService jobService;

    public CandidateController(CandidateService candidateService, SkillService skillService, CandidateSkillService candidateSkillService, ExperienceService experienceService, JobService jobService) {
        this.candidateService = candidateService;
        this.skillService = skillService;
        this.candidateSkillService = candidateSkillService;
        this.experienceService = experienceService;
        this.jobService = jobService;
    }

    @GetMapping
    @RequestMapping("/find-job")
    public String findJob(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));
        List<Job> jobs = jobService.suggestJob(candidate.get().getId());
        List<CandidateSkill> candidateSkills = candidateSkillService.findByCanId(candidate.get().getId());
        Map<Job, String> suggestions = new HashMap<>();
        for (Job job : jobs) {
            List<JobSkill> jobSkills = job.getJobSkills();
            String suggest = "Suggest: ";
            for (JobSkill jobSkill : jobSkills) {
                boolean exists = false;
                for (CandidateSkill candidateSkill : candidateSkills) {
                    if(candidateSkill.getSkill() == jobSkill.getSkill()) {
                        exists = true;
                        System.out.println("jobSkill.getSkillLevel().getLevel(): "+jobSkill.getSkillLevel());
                        System.out.println("jobSkill.getSkillLevel().getLevel(): "+jobSkill.getSkillLevel().getLevel());
                        System.out.println("candidateSkill.getSkillLevel().getLevel(): "+candidateSkill.getSkillLevel());
                        System.out.println("candidateSkill.getSkillLevel().getLevel(): "+candidateSkill.getSkillLevel().getLevel());
                        if(candidateSkill.getSkillLevel() != SkillLevel.MASTER
                        && candidateSkill.getSkillLevel().getLevel() < jobSkill.getSkillLevel().getLevel()) {
                            suggest += "\n You should be improve skill " + candidateSkill.getSkill().getSkillName()+ " to level "+ jobSkill.getSkillLevel();
                        }
                        break;
                    }
                }
                if(!exists) {
                    suggest +="\n You should be learn new skill " + jobSkill.getSkill().getSkillName()+ " to level "+ jobSkill.getSkillLevel();
                }
                suggestions.put(job, suggest);
            }
        }
        model.addAttribute("suggestions", suggestions);
        return "candidates/FindJob";
    }
    @GetMapping
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));
        model.addAttribute("candidate", candidate.get());
        model.addAttribute("experiences", experienceService.findByCanId(candidate.get().getId()));
        model.addAttribute("skills", candidateSkillService.findByCanId(candidate.get().getId()));
        return "candidates/Home";

    }
    @PostMapping
    @RequestMapping("/add-skill")
    public String create(EntityRequestAddSkill entityRequestAddSkill, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));
        if(candidate.isEmpty()){
            return "redirect:/";
        }
        Map<String, Object> response = new HashMap<>();
        model.addAttribute("candidate", candidate.get());
        if(entityRequestAddSkill.candidateSkillId > 0){
            Optional<Skill> skill = skillService.findById(entityRequestAddSkill.candidateSkillId);
            if(skill.isPresent()){
                if(entityRequestAddSkill.skillLevel != null){

                    CandidateSkillId candidateSkillId = new CandidateSkillId();
                    candidateSkillId.setCanId(candidate.get().getId());
                    candidateSkillId.setSkillId(skill.get().getId());

                    CandidateSkill candidateSkill = new CandidateSkill(candidate.get(), skill.get(), entityRequestAddSkill.moreInfo, entityRequestAddSkill.skillLevel);
                    candidateSkill.setId(candidateSkillId);
                    model.addAttribute("skill",  candidateSkillService.save(candidateSkill));
                    model.addAttribute("message","Add skill successfully");

                }else{
                    model.addAttribute("error","Please, choose skill level");
                }
            }else{
                model.addAttribute("error","Skill not found");

            }
        }

        model.addAttribute("experiences", experienceService.findByCanId(candidate.get().getId()));
        model.addAttribute("skills", candidateSkillService.findByCanId(candidate.get().getId()));
        return "candidates/Home";

    }
    @PostMapping
    @RequestMapping("/add-experience")
    public String addEnperience(EntityRequestAddExperience entityRequestAddExperience,Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));

        if(candidate.isEmpty()){
            return "redirect:/";
        }

        model.addAttribute("candidate", candidate.get());
        if(!entityRequestAddExperience.companyName.isEmpty()){
            if(!entityRequestAddExperience.role.trim().isEmpty()){
                if(entityRequestAddExperience.fromDate != null){
                    if(entityRequestAddExperience.toDate != null){
                        LocalDate currentDate = LocalDate.now();
                        if(entityRequestAddExperience.fromDate.isBefore(currentDate) && entityRequestAddExperience.toDate.isBefore(currentDate)){
                            if(entityRequestAddExperience.fromDate().isBefore(entityRequestAddExperience.toDate)){
                                if(!entityRequestAddExperience.workDescription.trim().isEmpty()){
                                    if(experienceService.isExperienceOverlap(entityRequestAddExperience.fromDate, entityRequestAddExperience.toDate)){
                                        model.addAttribute("error","Experience overlap");
                                    }else{
                                        Experience experience = new Experience(entityRequestAddExperience.companyName,entityRequestAddExperience.fromDate, entityRequestAddExperience.toDate, entityRequestAddExperience.role, entityRequestAddExperience.workDescription);
                                        experience.setCandidate(candidate.get());
                                        experienceService.save(experience);
                                        model.addAttribute("message","Add experience successfully");
                                    }
                                }else{
                                    model.addAttribute("error","Please, enter experience description");
                                }
                            }else{
                                model.addAttribute("error","from date must than to date");
                            }
                        }else{
                            model.addAttribute("error","from date and to date must than to current date");
                        }
                    }else{
                        model.addAttribute("error","Please, enter to date");
                    }
                }else{
                    model.addAttribute("error","Please, enter from date");
                }
            }else{
                model.addAttribute("error","Please, enter role");
            }
        }else{
            model.addAttribute("error","Please, enter company name");
        }
        model.addAttribute("experiences", experienceService.findByCanId(candidate.get().getId()));
        model.addAttribute("skills", candidateSkillService.findByCanId(candidate.get().getId()));
        return "candidates/Home";
    }

    private record EntityRequestAddExperience(String companyName, String role, LocalDate fromDate, LocalDate toDate, String workDescription) {}
    private record EntityRequestAddSkill(long candidateSkillId, SkillLevel skillLevel, String moreInfo){}
}
