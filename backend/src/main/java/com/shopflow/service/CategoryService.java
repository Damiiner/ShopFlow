package com.shopflow.service;

import com.shopflow.domain.Category;
import com.shopflow.dto.category.CategoryRequest;
import com.shopflow.dto.category.CategoryTreeResponse;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> tree() {
        var roots = categoryRepository.findByParentIsNullOrderByNameAsc();
        return roots.stream().map(this::toTree).toList();
    }

    private CategoryTreeResponse toTree(Category c) {
        var children = c.getChildren().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(this::toTree)
                .toList();
        return CategoryTreeResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .children(children)
                .build();
    }

    @Transactional
    public CategoryTreeResponse create(CategoryRequest req) {
        var cat = new Category();
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        if (req.getParentId() != null) {
            cat.setParent(categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Parent not found")));
        }
        cat = categoryRepository.save(cat);
        return toTree(cat);
    }

    @Transactional
    public CategoryTreeResponse update(Long id, CategoryRequest req) {
        var cat = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Category not found"));
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        if (req.getParentId() != null) {
            if (req.getParentId().equals(id)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot set self as parent");
            }
            cat.setParent(categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Parent not found")));
        } else {
            cat.setParent(null);
        }
        categoryRepository.save(cat);
        return toTree(cat);
    }

    @Transactional
    public void delete(Long id) {
        var cat = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Category not found"));
        if (!cat.getChildren().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Remove subcategories first");
        }
        categoryRepository.delete(cat);
    }
}
