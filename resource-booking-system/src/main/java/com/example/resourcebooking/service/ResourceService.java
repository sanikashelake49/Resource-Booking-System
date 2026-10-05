package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.PageResponse;
import com.example.resourcebooking.dto.ResourceRequest;
import com.example.resourcebooking.dto.ResourceResponse;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;

    public ResourceResponse create(ResourceRequest request) {
        com.example.resourcebooking.entity.Resource resource = com.example.resourcebooking.entity.Resource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .location(request.getLocation())
                .price(request.getPrice())
                .available(request.getAvailable() == null ? Boolean.TRUE : request.getAvailable())
                .build();

        com.example.resourcebooking.entity.Resource saved = resourceRepository.save(resource);
        log.info("Created resource id={} name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> getAll(Pageable pageable) {
        Page<ResourceResponse> page = resourceRepository.findAll(pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public ResourceResponse update(Long id, ResourceRequest request) {
        com.example.resourcebooking.entity.Resource resource = findEntity(id);
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setLocation(request.getLocation());
        resource.setPrice(request.getPrice());
        if (request.getAvailable() != null) {
            resource.setAvailable(request.getAvailable());
        }
        com.example.resourcebooking.entity.Resource saved = resourceRepository.save(resource);
        log.info("Updated resource id={}", saved.getId());
        return toResponse(saved);
    }

    public void delete(Long id) {
        com.example.resourcebooking.entity.Resource resource = findEntity(id);
        resourceRepository.delete(resource);
        log.info("Deleted resource id={}", id);
    }

    private com.example.resourcebooking.entity.Resource findEntity(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    private ResourceResponse toResponse(com.example.resourcebooking.entity.Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .location(resource.getLocation())
                .price(resource.getPrice())
                .available(resource.getAvailable())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
