package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swd392.eventmanagement.model.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByProviderId(String providerUserId);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    @Query(value = "SELECT COUNT(DISTINCT ur.user_id) FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_STUDENT'",nativeQuery = true)
    long countStudents();

    @Query(value= "SELECT COUNT(DISTINCT ur.user_id) FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_LECTURER'",nativeQuery = true)
    long countLectures();
}