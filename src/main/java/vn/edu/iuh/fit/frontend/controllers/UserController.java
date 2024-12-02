package vn.edu.iuh.fit.frontend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class UserController {
//    private final AuthenticationManager authenticationManager;
//
//    public LoginController(AuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//    }



    //    @PostMapping("/login")
//    public String login(@RequestBody LoginRequest loginRequest) {
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password);
//        Authentication authentication = authenticationManager.authenticate(authenticationToken);
//        if (authentication != null) {
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            return "redirect:/";
//        }
//        return "redirect:/login?error=true";
//    }
    private record LoginRequest(String username, String password) {}
    private record RegisterCandidateRequest(String username, LocalDate dob, String email, String password) {}
}
