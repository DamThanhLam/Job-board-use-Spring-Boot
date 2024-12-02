package vn.edu.iuh.fit.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.backend.models.Experience;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByCandidate_Id(long id);
    @Query("SELECT e FROM Experience e WHERE :to >= e.fromDate and :to <= e.toDate OR :from >= e.fromDate AND :from <= e.toDate OR :from <= e.fromDate AND :to >= e.toDate")
    Optional<Experience> findExperienceOverlap(@Param("to") LocalDate to, @Param("from") LocalDate from);
}
