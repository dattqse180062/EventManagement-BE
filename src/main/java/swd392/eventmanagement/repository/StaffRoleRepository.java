package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.StaffRole;

import java.util.Optional;

public interface StaffRoleRepository extends JpaRepository<StaffRole, Long> {
    Optional<StaffRole> findByStaffRoleName(String staffRoleName);

    boolean existsByStaffRoleName(String staffRoleName);
}
