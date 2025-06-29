package com.example.demo3.mapper;

import com.example.demo3.dto.*;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    private final CategoryMapper categoryMapper;

    public ProductMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public ProductWithoutCategoryDTO toProductWithoutCategoryDTO(ProductEntity entity) {
        ProductWithoutCategoryDTO dto = new ProductWithoutCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setActive(entity.getIsActive());
        return dto;
    }

    public ProductBasicDTO toDTO(ProductEntity entity) {
        ProductBasicDTO dto = new ProductBasicDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setActive(entity.getIsActive());
        CategoryBasicDTO categoryDTO = new CategoryBasicDTO();
        categoryDTO.setId(entity.getCategory().getId());
        categoryDTO.setName(entity.getCategory().getName());
        categoryDTO.setDescription(entity.getCategory().getDescription());
        categoryDTO.setChildren(categoryMapper.toChildrenDTOList(entity.getCategory().getChildren()));
        categoryDTO.setParent(entity.getCategory().getParent() != null
                ? entity.getCategory().getParent().getId()
                : null);

        dto.setCategory(categoryDTO);
        return dto;
    }

    public List<ProductBasicDTO> toDTOList(List<ProductEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductBasicDTO> toDTOList(Page<ProductEntity> entities) {
        return entities.map(this::toDTO);
    }

    public PageableResponseProducts createPageableResponseProducts(Page<ProductEntity> pageable) {
        PageableResponseProducts pageableResponseProducts = new PageableResponseProducts();
        pageableResponseProducts.setContent(pageable.getContent()
                .stream()
                .map(this::toDTO)
                .toList());
        pageableResponseProducts.setFirst(pageable.isFirst());
        pageableResponseProducts.setLast(pageable.isLast());
        pageableResponseProducts.setTotalElements(pageable.getTotalElements());
        pageableResponseProducts.setTotalPages(pageable.getTotalPages());
        pageableResponseProducts.setPageSize(pageable.getSize());
        pageableResponseProducts.setPageNumber(pageable.getNumber());
        return pageableResponseProducts;
    }

    public ProductEntity createProduct(ProductRequestDTO product, CategoryEntity category) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setCategory(category);
        productEntity.setStockQuantity(product.getStockQuantity());
        productEntity.setDescription(product.getDescription());
        productEntity.setSku(product.getSku());
        productEntity.setPrice(product.getPrice());
        productEntity.setIsActive(product.getStockQuantity() > 0);
        return productEntity;
    }

    public ProductEntity updateProduct(UpdateProductRequestDTO productRequest, ProductEntity productEntity) {
        productEntity.setName((productRequest.getName() == null)
                ? productEntity.getName()
                : productRequest.getName());
        productEntity.setDescription((productRequest.getDescription() == null)
                ? productEntity.getDescription()
                : productRequest.getDescription());
        productEntity.setPrice((productRequest.getPrice() == null)
                ? productEntity.getPrice()
                : productRequest.getPrice());
        productEntity.setStockQuantity((productRequest.getStockQuantity() == null)
                ? productEntity.getStockQuantity()
                : productRequest.getStockQuantity());
        productEntity.setSku((productRequest.getSku() == null)
                ? productEntity.getSku()
                : productRequest.getSku());
        productEntity.setSku((productRequest.getSku() == null)
                ? productEntity.getSku()
                : productRequest.getSku());
        return productEntity;
    }

    public ProductEntity updateProductCategory(CategoryEntity category, ProductEntity productEntity) {
        productEntity.setCategory(category);
        return productEntity;
    }

    public PageableResponseGetProductsByCategory createPageableResponseGetProductsByCategory(Page<ProductEntity> productPage) {
        PageableResponseGetProductsByCategory response = new PageableResponseGetProductsByCategory();
        response.setContent(productPage.getContent()
                .stream()
                .map(this::toProductWithoutCategoryDTO)
                .toList());
        response.setLast(productPage.isLast());
        response.setFirst(productPage.isFirst());
        response.setPageNumber(productPage.getPageable().getPageNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setPageSize(productPage.getPageable().getPageSize());
        response.setTotalElements(productPage.getTotalElements());
        return response;
    }
}
