package swd392.eventmanagement.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd392.eventmanagement.exception.TagNotFoundException;
import swd392.eventmanagement.exception.TagProcessingException;
import swd392.eventmanagement.exception.ValidationException;
import swd392.eventmanagement.model.dto.request.TagRequest;
import swd392.eventmanagement.model.dto.response.TagShowDTO;
import swd392.eventmanagement.model.entity.Tag;
import swd392.eventmanagement.model.mapper.TagMapper;
import swd392.eventmanagement.repository.TagRepository;
import swd392.eventmanagement.service.TagService;

@Service
public class TagServiceImpl implements TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional
    @Override
    public TagShowDTO createTag(TagRequest request) {
        logger.info("Creating new tag with name: " + request.getName());

        try{
            //Check if tag name already exists
            if(tagRepository.existsByName(request.getName())) {
                throw new ValidationException("Tag name already exists");
            }

            //Map from request DTO to entity
            Tag tag = tagMapper.toEntity(request);
            tag.setIsActive(true);

            //Save entity
            Tag savedTag = tagRepository.save(tag);

            //Map entity to response DTO
            TagShowDTO response = tagMapper.toDTO(savedTag);
            logger.info("Tag created successfully - ID: {}, Name: {}", savedTag.getId(), savedTag.getName());

            return response;

        } catch (ValidationException e) {
            logger.warn("Validation error while creating tag: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating tag", e);
            throw new TagProcessingException("Failed to create tag", e);
        }
    }

    @Override
    public List<TagShowDTO> getActiveTags() {
        logger.info("Getting all active tags");
        try {
            List<Tag> activeTags = tagRepository.findByIsActiveTrue();

            if (activeTags.isEmpty()) {
                logger.info("No active tags found");
                throw new TagNotFoundException("No active tags found in the system");
            }

            logger.info("Found {} active tags", activeTags.size());
            return tagMapper.toListDTOs(activeTags);
        } catch (TagNotFoundException e) {
            // Just rethrow TagNotFoundException to be handled by the global exception
            // handler
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving active tags", e);
            throw new TagProcessingException("Failed to retrieve active tags", e);
        }
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        logger.info("Deleting tag with ID: {}", tagId);

        try {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new TagNotFoundException("Tag not found with ID: " + tagId));

            tag.setIsActive(false);
            tagRepository.save(tag);

            logger.info("Tag with ID: {} has been marked as inactive", tagId);
        } catch (TagNotFoundException e) {
            logger.warn("Delete failed: {}", e.getMessage());
            throw e; // propagate để controller xử lý trả về 404
        } catch (Exception e) {
            logger.error("Unexpected error occurred while deleting tag", e);
            throw new TagProcessingException("Failed to delete tag", e);
        }
    }
    @Override
    public void updateTag(Long id, TagRequest request) {
        logger.info("Updating tag with id: {} " + id);
        try{
            //Find tag by ID
            Tag tag = tagRepository.findById(id)
                    .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));

            //Check if name already exist and not the same tag
            if(tagRepository.existsByName(request.getName())&& !tag.getName().equals(request.getName())) {
                throw new ValidationException("Tag name already exists");
            }

            //Update fields
            tag.setName(request.getName());
            tag.setDescription(request.getDescription());

            //Save updated tag
            tagRepository.save(tag);

            logger.info("Tag updated successfully - ID: {}, Name: {}", tag.getId(), tag.getName());
        } catch (ValidationException e) {
            logger.warn("Validation failed while updating tag: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while updating tag", e);
            throw new TagProcessingException("Failed to update tag", e);
        }
    }

}
