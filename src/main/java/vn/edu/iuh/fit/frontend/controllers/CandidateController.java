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
import vn.edu.iuh.fit.backend.models.Candidate;
import vn.edu.iuh.fit.backend.models.CandidateSkill;
import vn.edu.iuh.fit.backend.models.Experience;
import vn.edu.iuh.fit.backend.models.Skill;
import vn.edu.iuh.fit.backend.services.CandidateService;
import vn.edu.iuh.fit.backend.services.CandidateSkillService;
import vn.edu.iuh.fit.backend.services.ExperienceService;
import vn.edu.iuh.fit.backend.services.SkillService;

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

    public CandidateController(CandidateService candidateService, SkillService skillService, CandidateSkillService candidateSkillService, ExperienceService experienceService) {
        this.candidateService = candidateService;
        this.skillService = skillService;
        this.candidateSkillService = candidateSkillService;
        this.experienceService = experienceService;
    }

    @GetMapping
    @RequestMapping("/find-job")
    public String findJob(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        Optional<Candidate> candidate = candidateService.findByEmail(user.getAttribute("email"));
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
