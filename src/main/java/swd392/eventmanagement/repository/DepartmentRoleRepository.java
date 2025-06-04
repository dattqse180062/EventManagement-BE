package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.DepartmentRole;

import java.util.Optional;

public interface DepartmentRoleRepository extends JpaRepository<DepartmentRole, Long> {
    Optional<DepartmentRole> findByName(String name);
}
