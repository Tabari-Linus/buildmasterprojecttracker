package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {


    Optional<Developer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Developer> findByNameContainingIgnoreCase(String name);

    List<Developer> findBySkillsContainingIgnoreCase(String skill);

    @Query("SELECT d FROM Developer d WHERE LOWER(d.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Developer> findDevelopersBySkill(@Param("skill") String skill);

    @Query("SELECT COUNT(d) FROM Developer d")
    long countAllDevelopers();
}
