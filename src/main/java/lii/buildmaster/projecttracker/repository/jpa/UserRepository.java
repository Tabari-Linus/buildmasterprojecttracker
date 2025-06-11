package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


}
