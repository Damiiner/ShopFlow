package com.shopflow.controller;

import com.shopflow.dto.category.CategoryRequest;
import com.shopflow.dto.category.CategoryTreeResponse;
import com.shopflow.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryTreeResponse> tree() {
        return categoryService.tree();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryTreeResponse create(@Valid @RequestBody CategoryRequest req) {
        return categoryService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryTreeResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return categoryService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
