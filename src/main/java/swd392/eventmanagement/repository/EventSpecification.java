package swd392.eventmanagement.repository;

import org.springframework.data.jpa.domain.Specification;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.model.entity.Event;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> filter(
            String name,
            List<Long> tagIds,
            Long typeId,
            TargetAudience targetAudience,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            EventMode mode,
            Long departmentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            // Filter by tags
            if (tagIds != null && !tagIds.isEmpty()) {
                Join<Object, Object> tags = root.join("tags", JoinType.LEFT);
                predicates.add(tags.get("id").in(tagIds));

                // Important: Avoid duplicates due to multiple tag matches
                query.distinct(true);
            }

            // Filter by type
            if (typeId != null) {
                predicates.add(cb.equal(root.get("type").get("id"), typeId));
            }

            // Filter by target audience
            if (targetAudience != null) {
                if (targetAudience == TargetAudience.STUDENT) {
                    predicates.add(root.get("targetAudience").in(TargetAudience.STUDENT, TargetAudience.BOTH));
                } else if (targetAudience == TargetAudience.LECTURER) {
                    predicates.add(root.get("targetAudience").in(TargetAudience.LECTURER, TargetAudience.BOTH));
                } else {
                    predicates.add(cb.equal(root.get("targetAudience"), targetAudience));
                }
            }

            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(root.get("status").in(
                        EventStatus.PUBLISHED,
                        EventStatus.BLOCKED,
                        EventStatus.CLOSED,
                        EventStatus.COMPLETED));
            }

            // Filter by start time range
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), to));
            } // Filter by mode
            if (mode != null) {
                predicates.add(cb.equal(root.get("mode"), mode));
            }

            // Filter by department
            if (departmentId != null && departmentId > 0) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
