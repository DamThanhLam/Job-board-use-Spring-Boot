package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.backend.models.Company;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByEmail(String email);
}
