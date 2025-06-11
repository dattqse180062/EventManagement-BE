package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.entity.UserDepartmentRole;

import java.util.List;
import java.util.Optional;

public interface UserDepartmentRoleRepository extends JpaRepository<UserDepartmentRole, Long> {
    List<UserDepartmentRole> findByUser(User user);

    List<UserDepartmentRole> findByDepartment(Department department);

    List<UserDepartmentRole> findByDepartmentRole(DepartmentRole departmentRole);

    Optional<UserDepartmentRole> findByUserAndDepartmentAndDepartmentRole(
            User user, Department department, DepartmentRole departmentRole);

    boolean existsByUserAndDepartmentAndDepartmentRole(User user, Department department, DepartmentRole departmentRole);


    Optional<UserDepartmentRole> findByUserAndDepartment(User user, Department department);

}
