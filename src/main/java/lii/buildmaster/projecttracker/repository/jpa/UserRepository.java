package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.dto.response.UserResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.EntityGraph; // IMPORTANT: Add this import

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByRolesName(RoleName roles_name, Pageable pageable);

    // Existing findByUsername and findByEmail (can coexist with EntityGraph versions)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // This method will be replaced by the @EntityGraph version in AuthService
    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") RoleName roleName);

    long countByEnabled(boolean b);

    // --- NEW METHODS WITH @EntityGraph ---

    // Explicit @Query to prevent Spring Data JPA from misinterpreting "WithDeveloper" as a property.
    @EntityGraph(attributePaths = {"developer"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithDeveloper(@Param("id") Long id);

    // Explicit @Query to prevent Spring Data JPA from misinterpreting "WithDeveloper" as a property.
    // This allows us to use @EntityGraph for eager fetching of the 'developer' association.
    @EntityGraph(attributePaths = {"developer"})
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithDeveloper(@Param("username") String username);

    // Explicit @Query to prevent Spring Data JPA from misinterpreting "WithDeveloper" as a property.
    // This allows us to use @EntityGraph for eager fetching of the 'developer' association.
    @EntityGraph(attributePaths = {"developer"})
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findUserWithDeveloperByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}