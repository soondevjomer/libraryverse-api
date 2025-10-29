package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.TagDto;
import com.soondevjomer.libraryverse.model.Tag;
import com.soondevjomer.libraryverse.repository.TagRepository;
import com.soondevjomer.libraryverse.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    @Override
    public TagDto createTag(TagDto tagDto) {

        Tag tagCreated = tagRepository.save(Tag.builder().name(tagDto.getName()).build());

        return new TagDto(tagCreated.getId(), tagCreated.getName());
    }
}
