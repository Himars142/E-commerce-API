package com.example.demo3.service.impl;

import com.example.demo3.dto.*;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.ProductMapper;
import com.example.demo3.repository.ProductsRepository;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.CategoryService;
import com.example.demo3.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductsRepository productsRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final AuthService authService;

    public ProductServiceImpl(ProductsRepository productsRepository,
                              ProductMapper productMapper,
                              CategoryService categoryService,
                              AuthService authService) {
        this.productsRepository = productsRepository;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @Override
    public ProductBasicDTO getProductById(Long id) {
        return productMapper.toDTO(getProduct(id));
    }

    @Override
    public PageableResponseProducts getAllProducts(int page, int size) {
        Page<ProductEntity> pageable = productsRepository.findAll(PageRequest.of(page, size));
        return productMapper.createPageableResponseProducts(pageable);
    }

    @Transactional
    @Override
    public void addProduct(String token, ProductRequestDTO product) {
        authService.checkIsUserAdmin(token);
        CategoryEntity category = categoryService.getCategoryById(product.getCategoryId());
        productsRepository.findBySku(product.getSku())
                .ifPresent(sku -> {
                    throw new BadRequestException("Sku must be unique");
                });
        productsRepository.save(productMapper.createProduct(product, category));
    }

    @Transactional
    @Override
    public void updateProduct(String token, UpdateProductRequestDTO request) {
        authService.checkIsUserAdmin(token);
        ProductEntity product = productsRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with ID:" + request.getId()));
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            productsRepository.findBySku(product.getSku())
                    .ifPresent(sku -> {
                        throw new BadRequestException("Sku must be unique");
                    });
        }
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryService.getCategoryById(request.getCategoryId());
            productMapper.updateProductCategory(category, product);
        }
        productsRepository.save(productMapper.updateProduct(request, product));
    }

    @Transactional
    @Override
    public void changeIsActive(String token, Long id) {
        authService.checkIsUserAdmin(token);
        ProductEntity product = productsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID:" + id));
        product.setIsActive(!product.getIsActive());
        productsRepository.save(product);
    }

    @Override
    public ProductEntity validateAndGetProduct(Long productId) {
        ProductEntity product = getProduct(productId);
        if (!product.getIsActive()) {
            throw new BadRequestException("Product " + product.getName() + " is disabled");
        }
        if (product.getStockQuantity() == 0) {
            changeIsActive(productId);
            throw new BadRequestException("Product disabled");
        }
        return product;
    }

    @Override
    public ProductEntity getProduct(Long productId) {
        return productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with this id don`t exist. ID:" + productId));
    }

    @Override
    public PageableResponseGetProductsByCategory getAllProductByCategoryId(Long categoryId, int page, int size) {
        Page<ProductEntity> productPage = productsRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
        return productMapper.createPageableResponseGetProductsByCategory(productPage);
    }

    @Transactional
    @Override
    public void increaseStockForOrderItems(List<OrderItemEntity> orderItems) {
        for (OrderItemEntity orderItem : orderItems) {
            ProductEntity product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
        }
        productsRepository.saveAll(orderItems.stream().map(OrderItemEntity::getProduct).toList());
    }

    @Transactional
    @Override
    public void decreaseStockForOrderItems(List<CartItemEntity> cartItems) {
        for (CartItemEntity cartItem : cartItems) {
            ProductEntity product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }
        productsRepository.saveAll(cartItems.stream().map(CartItemEntity::getProduct).toList());
    }

    @Override
    public void validateProductsForOrder(List<CartItemEntity> cartItems) {
        for (CartItemEntity item : cartItems) {
            ProductEntity product = productsRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product with ID " + item.getProduct().getId() + " not found."));
            if (!product.getIsActive()) {
                throw new BadRequestException("Product " + product.getName() + " is disabled");
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Not enough '" + product.getName() + "' in stock. Available: " + product.getStockQuantity() + ", requested: " + item.getQuantity());
            }
        }
    }

    @Transactional
    private void changeIsActive(Long id) {
        ProductEntity product = getProduct(id);
        product.setIsActive(!product.getIsActive());
        product.setUpdatedAt(LocalDateTime.now());
        productsRepository.save(product);
    }
}
