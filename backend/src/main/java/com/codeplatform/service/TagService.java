package com.codeplatform.service;

import com.codeplatform.entity.Tag;
import com.codeplatform.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Set<Tag> resolveOrCreate(Set<String> names) {
        Set<Tag> tags = new LinkedHashSet<>();
        for (String rawName : names) {
            String name = rawName.trim().toLowerCase();
            if (name.isEmpty()) {
                continue;
            }
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            tags.add(tag);
        }
        return tags;
    }
}
