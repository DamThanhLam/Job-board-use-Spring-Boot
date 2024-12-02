package vn.edu.iuh.fit.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.backend.services.CandidateService;
import vn.edu.iuh.fit.backend.services.CompanyService;

import java.io.IOException;

@Component
public class RedirectIfLoggedInFilter extends OncePerRequestFilter {
    private final CandidateService candidateService;
    private final CompanyService companyService;

    public RedirectIfLoggedInFilter(CandidateService candidateService, CompanyService companyService) {
        this.candidateService = candidateService;
        this.companyService = companyService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.isAuthenticated()){
            if (request.getRequestURI().equals("/login")|| request.getRequestURI().contains("/login")) {
                // Người dùng đã đăng nhập, chuyển hướng đến trang chủ
                response.sendRedirect("/");
            } else if(request.getRequestURI().equals("/")){
                OAuth2User user = (OAuth2User) auth.getPrincipal();
                if(candidateService.findByEmail(user.getAttribute("email")).isPresent()){
                    response.sendRedirect("/candidate");
                }else if(companyService.findByEmail(user.getAttribute("email")).isPresent()){
                    response.sendRedirect("/company");
                }else{
                    filterChain.doFilter(request, response);
                }
            }else {
                filterChain.doFilter(request, response);  // Tiếp tục lọc các request khác
            }
        }else{
            response.sendRedirect("/login");

        }


    }
}
