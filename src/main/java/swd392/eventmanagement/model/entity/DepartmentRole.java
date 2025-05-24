package swd392.eventmanagement.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "department_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "departmentRole")
    private Set<UserDepartmentRole> userDepartmentRoles;
}
