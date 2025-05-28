package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.model.dto.request.TagRequest;
import swd392.eventmanagement.model.entity.Tag;
import swd392.eventmanagement.repository.TagRepository;
import swd392.eventmanagement.service.TagService;
@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;


    @Override
    public void createTag(TagRequest request) {
    // Kiểm tra nếu tên tag đã tồn tại
        if(tagRepository.existsByName(request.getName())){
            throw new RuntimeException("Tag đã tồn tại");
        }
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setIsActive(true);
        tagRepository.save(tag);
    }

}
