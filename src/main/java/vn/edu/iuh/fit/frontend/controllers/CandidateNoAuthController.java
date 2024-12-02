package vn.edu.iuh.fit.frontend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.iuh.fit.backend.models.Candidate;
import vn.edu.iuh.fit.backend.services.CandidateService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CandidateNoAuthController {
    private final CandidateService candidateService;

    public CandidateNoAuthController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }
    @GetMapping
    @RequestMapping("/list")
    public String listCandidates(Model model) {
        model.addAttribute("candidates",candidateService.findAll());
        return "candidates/candidates";
    }
    @GetMapping("/candidates")
    public String showCandidateListPaging(Model model,
                                          @RequestParam("page") Optional<Integer> page,
                                          @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<Candidate> candidatePage= candidateService.findAll(currentPage - 1,pageSize,"id","asc");
        model.addAttribute("candidatePage", candidatePage);
        int totalPages = candidatePage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "candidates/candidates-paging";
    }
}
