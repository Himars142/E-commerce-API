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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

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
        logger.info("Attempt to get product by id {}", id);
        ProductBasicDTO response = productMapper.toDTO(getProduct(id));
        logger.info("Success to get product id {}", response.getId());
        return response;
    }

    @Override
    public PageableResponseProducts getAllProducts(int page, int size) {
        logger.info("Attempt to get all products pageable. Page: {}, size: {}", page, size);
        Page<ProductEntity> pageable = productsRepository.findAll(PageRequest.of(page, size));
        PageableResponseProducts response = productMapper.createPageableResponseProducts(pageable);
        logger.info("Success attempt to get all products pageable. Page: {}, size: {}", page, size);
        return response;
    }

    @Transactional
    @Override
    public void addProduct(String token, ProductRequestDTO product) {
        logger.info("Attempt to add product: {}", product.toString());
        authService.checkIsUserAdmin(token);
        CategoryEntity category = categoryService.getCategoryById(product.getCategoryId());
        productsRepository.findBySku(product.getSku())
                .ifPresent(sku -> {
                    throw new BadRequestException("Sku must be unique");
                });
        productsRepository.save(productMapper.createProduct(product, category));
        logger.info("Success product created product: {}", product);
    }

    @Transactional
    @Override
    public void updateProduct(String token, UpdateProductRequestDTO request) {
        logger.info("Attempt to update request: {}", request);
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
        logger.info("Success product updated productId:{}", product.getId());
    }

    @Transactional
    @Override
    public void changeIsActive(String token, Long id) {
        logger.info("Attempt to change is active in productId: {}", id);
        authService.checkIsUserAdmin(token);
        ProductEntity product = productsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID:" + id));
        product.setIsActive(!product.getIsActive());
        productsRepository.save(product);
        logger.info("Success change productId: {} active is {}", id, product.getIsActive());
    }

    @Override
    public ProductEntity validateAndGetProduct(Long productId) {
        logger.info("Attempt to validate and get product productId: {}", productId);
        ProductEntity product = getProduct(productId);
        if (!product.getIsActive()) {
            throw new BadRequestException("Product " + product.getName() + " is disabled");
        }
        if (product.getStockQuantity() == 0) {
            changeIsActive(productId);
            throw new BadRequestException("Product disabled");
        }
        logger.info("Success product:{} ", product);
        return product;
    }

    @Override
    public ProductEntity getProduct(Long productId) {
        logger.debug("Attempt to get a productId: {}", productId);
        ProductEntity response = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with this id don`t exist. ID:" + productId));
        logger.debug("Success get productId: {}", response.getId());
        return response;
    }

    @Override
    public PageableResponseGetProductsByCategory getAllProductByCategoryId(Long categoryId, int page, int size) {
        logger.info("Attempt to get pageable response get all products by categoryId: {}. Page: {}, size: {}",
                categoryId, page, size);
        Page<ProductEntity> productPage = productsRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
        PageableResponseGetProductsByCategory response = productMapper.createPageableResponseGetProductsByCategory(productPage);
        logger.info("Success get pageable response get all products by categoryId: {}. Page: {}, size: {}",
                categoryId, page, size);
        return response;
    }

    @Transactional
    @Override
    public void increaseStockForOrderItems(List<OrderItemEntity> orderItems) {
        logger.debug("Attempt to increase stock for order items");
        for (OrderItemEntity orderItem : orderItems) {
            ProductEntity product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
        }
        productsRepository.saveAll(orderItems.stream().map(OrderItemEntity::getProduct).toList());
        logger.debug("Success increase stock for order items");
    }

    @Transactional
    @Override
    public void decreaseStockForOrderItems(List<CartItemEntity> cartItems) {
        logger.debug("Attempt to decrease stock for order items");
        for (CartItemEntity cartItem : cartItems) {
            ProductEntity product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }
        productsRepository.saveAll(cartItems.stream().map(CartItemEntity::getProduct).toList());
        logger.debug("Success attempt to decrease stock for order items");
    }

    @Override
    public void validateProductsForOrder(List<CartItemEntity> cartItems) {
        logger.debug("Attempt to validate products for order");
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
        logger.debug("Success validate products for order");
    }

    @Transactional
    private void changeIsActive(Long id) {
        logger.debug("Attempt to change is active for productId: {}", id);
        ProductEntity product = getProduct(id);
        product.setIsActive(!product.getIsActive());
        productsRepository.save(product);
        logger.debug("Success change is active for productId: {} to {}", id, product.getIsActive());
    }
}
