package swd392.eventmanagement.service.event.builder;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.exception.TagNotFoundException;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Tag;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.TagRepository;

@Component
public class TagBuilder {

    private final TagRepository tagRepository;
    private final EventRepository eventRepository;

    public TagBuilder(TagRepository tagRepository, EventRepository eventRepository) {
        this.tagRepository = tagRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Updates the tags for an event
     * Using PUT semantics - clears existing tags and adds new ones if provided
     * 
     * @param event  Event entity to update
     * @param tagIds List of tag IDs to associate with the event, can be null or
     *               empty
     * @throws TagNotFoundException If a tag ID doesn't exist
     */
    public void updateEventTags(Event event, Set<Long> tagIds) {
        // Clear existing tag associations in the join table
        event.getTags().clear();

        // Make sure to flush the changes to ensure the many-to-many associations are
        // removed
        eventRepository.flush();

        // Add new tags if provided
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));
                tags.add(tag);
            }
            event.setTags(tags);
        }
    }
}
