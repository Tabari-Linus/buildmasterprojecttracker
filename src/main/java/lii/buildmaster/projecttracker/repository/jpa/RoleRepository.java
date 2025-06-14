package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
    boolean existsByName(RoleName name);
}
