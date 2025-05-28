package swd392.eventmanagement.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> filter(
            String name,
            List<Long> tagIds,
            Long typeId,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Long departmentId) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (name != null && !name.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (tagIds != null && !tagIds.isEmpty()) {
                Join<Object, Object> tags = root.join("tags");
                predicate = cb.and(predicate, tags.get("id").in(tagIds));
            }
            if (typeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type").get("id"), typeId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startTime"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("startTime"), to));
            }
            if (departmentId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("department").get("id"), departmentId));
            }
            return predicate;
        };
    }
}