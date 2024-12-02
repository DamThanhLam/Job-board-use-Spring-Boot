package vn.edu.iuh.fit.frontend.controllers;

import com.neovisionaries.i18n.CountryCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.backend.models.Address;
import vn.edu.iuh.fit.backend.models.Candidate;
import vn.edu.iuh.fit.backend.models.Company;
import vn.edu.iuh.fit.backend.services.AddressService;
import vn.edu.iuh.fit.backend.services.CandidateService;
import vn.edu.iuh.fit.backend.services.CompanyService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/register")
public class RegisterController {
    private final CandidateService candidateService;
    private final AddressService addressService;
    private final CompanyService companyService;

    public RegisterController(CandidateService candidateService, AddressService addressService, CompanyService companyService) {
        this.candidateService = candidateService;
        this.addressService = addressService;
        this.companyService = companyService;
    }

    @GetMapping
    public String register(@RequestParam("role") String role, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated()){
            OAuth2User user = (OAuth2User) authentication.getPrincipal();
            model.addAttribute("email", user.getAttribute("email"));
            model.addAttribute("name", user.getAttribute("name"));

            List<CountryCode> countryCodes = Arrays.stream(CountryCode.values()).filter(countryCode -> !countryCode.name().equals("UNDEFINED")).toList();
            model.addAttribute("countryCodes", countryCodes);

            if(role.equals("candidate")){
                return "register/register_candidate";
            }else if(role.equals("company")){
                return "register/register_company";
            }
        }
        return "redirect:/login";
    }
    @PostMapping
    @RequestMapping("/company")
    public String registerCompany(CompanyRegisterRequest registerRequest, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        Optional<Candidate> candidateOptional = candidateService.findByEmail(user.getAttribute("email"));
        Optional<Company> companyOptional = companyService.findByEmail(user.getAttribute("email"));
        if(candidateOptional.isPresent()){
            return "redirect:/candidate";
        }
        if(companyOptional.isPresent()){
            return "redirect:/company";
        }

        if (!registerRequest.companyName.trim().isEmpty()) {
            if (!registerRequest.about.trim().isEmpty()) {
                String urlRegex = "^(https?://)?([\\w.-]+)?(\\.[a-z]{2,})?(:\\d{1,5})?(/.*)?$";
                if (registerRequest.webUrl.matches(urlRegex)) {
                    if (registerRequest.phone.matches("^[0-9]{8,15}$")) {
                        if (registerRequest.countryCode != null) {
                            if (!registerRequest.city.trim().isEmpty()) {
                                if (!registerRequest.zipcode.trim().isEmpty() && registerRequest.zipcode.trim().length() <=7) {
                                    if (!registerRequest.number.trim().isEmpty()) {
                                        if (!registerRequest.street.trim().isEmpty()) {
                                            Company company = new Company();
                                            Address address = new Address();
                                            address.setCity(registerRequest.city.trim());
                                            address.setCountry(registerRequest.countryCode);
                                            address.setZipcode(registerRequest.zipcode);
                                            address.setStreet(registerRequest.street.trim());
                                            address.setNumber(registerRequest.number.trim());

                                            company.setAddress(address);
                                            company.setAbout(registerRequest.about.trim());
                                            company.setPhone(registerRequest.phone.trim());
                                            company.setEmail(user.getAttribute("email"));
                                            company.setWebUrl(registerRequest.webUrl.trim());
                                            company.setName(registerRequest.companyName);

                                            addressService.save(address);
                                            companyService.save(company);
                                            // Thêm rule CANDIDATE vào quyền của người dùng sau khi đăng ký thành công
                                            List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
                                            updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_COMPANY"));


                                            // Kiểm tra nếu Authentication là OAuth2AuthenticationToken
                                            if (authentication instanceof OAuth2AuthenticationToken) {
                                                OAuth2AuthenticationToken oauth2AuthToken = (OAuth2AuthenticationToken) authentication;

                                                // Tạo lại OAuth2AuthenticationToken với các quyền mới
                                                OAuth2AuthenticationToken updatedAuthentication = new OAuth2AuthenticationToken(
                                                        oauth2AuthToken.getPrincipal(), updatedAuthorities, oauth2AuthToken.getAuthorizedClientRegistrationId());

                                                // Cập nhật lại SecurityContext với Authentication mới
                                                SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
                                            }

                                            return "redirect:/company";
                                        } else {
                                            model.addAttribute("error", "Street cannot be empty.");
                                        }
                                    } else {
                                        model.addAttribute("error", "Number cannot be empty.");
                                    }
                                } else {
                                    model.addAttribute("error", "Zipcode cannot be empty and length <= 7.");
                                }
                            } else {
                                model.addAttribute("error", "City cannot be empty.");
                            }
                        } else {
                            model.addAttribute("error", "Invalid country code.");
                        }
                    } else {
                        model.addAttribute("error", "Phone number must be between 8 to 15 digits.");
                    }
                } else {
                    model.addAttribute("error", "Invalid web URL format.");
                }
            } else {
                model.addAttribute("error", "About field cannot be empty.");
            }
        } else {
            model.addAttribute("error", "Company name cannot be empty.");
        }

        return "register/register_company";
    }

    @PostMapping
    @RequestMapping("/candidate")
    public String register(@ModelAttribute("candidate") CandidateRegisterRequest candidateRegisterRequest, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        Optional<Candidate> candidateOptional = candidateService.findByEmail(user.getAttribute("email"));
        Optional<Company> companyOptional = companyService.findByEmail(user.getAttribute("email"));
        if(candidateOptional.isPresent()){
            return "redirect:/candidate";
        }
        if(companyOptional.isPresent()){
            return "redirect:/company";
        }


        model.addAttribute("email", user.getAttribute("email"));
        model.addAttribute("name", user.getAttribute("name"));
        if(candidateRegisterRequest.phone.matches("^[0-9]{8,15}$")){
            if(candidateRegisterRequest.dob.isBefore(LocalDate.now())){
                LocalDate currentDate = LocalDate.now();
                Period age = Period.between(candidateRegisterRequest.dob, currentDate);
                if (age.getYears() >= 18) {
                    if(candidateRegisterRequest.country != null){
                        if(candidateRegisterRequest.city.trim().matches("[a-zA-Z]+")){
                            Address address = new Address();
                            address.setCity(candidateRegisterRequest.city);
                            address.setCountry(candidateRegisterRequest.country);
                            Candidate candidate = new Candidate();
                            candidate.setAddress(address);
                            candidate.setDob(candidateRegisterRequest.dob);
                            candidate.setPhone(candidateRegisterRequest.phone);
                            candidate.setFullName(user.getAttribute("name"));
                            candidate.setEmail(user.getAttribute("email"));
                            addressService.save(address);
                            candidateService.save(candidate);

                            // Thêm rule CANDIDATE vào quyền của người dùng sau khi đăng ký thành công
                            List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
                            updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_CANDIDATE"));


                            // Kiểm tra nếu Authentication là OAuth2AuthenticationToken
                            if (authentication instanceof OAuth2AuthenticationToken) {
                                OAuth2AuthenticationToken oauth2AuthToken = (OAuth2AuthenticationToken) authentication;

                                // Tạo lại OAuth2AuthenticationToken với các quyền mới
                                OAuth2AuthenticationToken updatedAuthentication = new OAuth2AuthenticationToken(
                                        oauth2AuthToken.getPrincipal(), updatedAuthorities, oauth2AuthToken.getAuthorizedClientRegistrationId());

                                // Cập nhật lại SecurityContext với Authentication mới
                                SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
                            }

                            return "redirect:/candidate";
                        }else{
                            model.addAttribute("error", "City name must have at least 1 character");
                            return "register/register_candidate";
                        }
                    }else{
                        model.addAttribute("error", "Country code > 0");
                        return "register/register_candidate";
                    }

                }else{
                    model.addAttribute("error", "You must be 18 years of age to register.");
                    return "register/register_candidate";
                }
            }
            else {
                model.addAttribute("error", "You must be 18 years of age to register.");
                return "register/register_candidate";
            }
        }else{
            model.addAttribute("error", "Phone number must have length > 7");
            return "register/register_candidate";
        }
    }
    public record CandidateRegisterRequest(LocalDate dob, CountryCode country,String city, String phone ){}
    public record CompanyRegisterRequest(String companyName, String about, String webUrl, String phone, CountryCode countryCode, String city, String zipcode, String number, String street){}
}
