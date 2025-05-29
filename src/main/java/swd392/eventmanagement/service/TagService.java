package swd392.eventmanagement.service;

import java.util.List;

import swd392.eventmanagement.model.dto.request.TagRequest;
import swd392.eventmanagement.model.dto.response.TagShowDTO;

public interface TagService {
    void createTag(TagRequest request);

    void updateTag(Long id, TagRequest request);

    List<TagShowDTO> getActiveTags();
}
