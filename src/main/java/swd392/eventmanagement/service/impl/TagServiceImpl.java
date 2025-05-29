package swd392.eventmanagement.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.TagNotFoundException;
import swd392.eventmanagement.exception.TagProcessingException;
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

    @Override
    public void createTag(TagRequest request) {
        // Kiểm tra nếu tên tag đã tồn tại
        if (tagRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tag đã tồn tại");
        }
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setIsActive(true);
        tagRepository.save(tag);
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
    public void updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag không tồn tại"));
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tagRepository.save(tag);
    }

}
