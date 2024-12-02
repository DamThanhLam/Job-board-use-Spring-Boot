package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Company;
import vn.edu.iuh.fit.backend.repositories.CompanyRepository;

import java.util.Optional;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;
    public void save(Company company) {
        companyRepository.save(company);
    }

    public Optional<Company> findByEmail(String email) {
        return companyRepository.findByEmail(email);
    }
}
