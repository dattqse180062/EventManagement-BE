package swd392.eventmanagement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staff_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_role_name", length = 100, nullable = false, unique = true)
    private String staffRoleName;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "staffRole")
    private Set<EventStaff> eventStaffs = new HashSet<>();
}
