package swd392.eventmanagement.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "event_capacity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCapacity {

    @EmbeddedId
    private EventCapacityId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class EventCapacityId implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "event_id")
        private Long eventId;

        @Column(name = "role_id")
        private Long roleId;
    }
}
