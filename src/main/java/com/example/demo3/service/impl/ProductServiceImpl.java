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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo3.utill.GenerateRequestID.generateRequestID;

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
    public ProductBasicDTO getProductById(Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get product by id. Request id: {}, user agent: {}, product id: {}.",
                requestId, userAgent, id);
        ProductBasicDTO response = productMapper.toDTO(getProduct(id, requestId));
        logger.info("Success to get product with id {}, request id: {}", response.getId(), requestId);
        return response;
    }

    @Override
    public PageableResponseProducts getAllProducts(int page, int size, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get all products. Request id: {}, user agent: {}, page: {}, size: {}.",
                requestId, userAgent, page, size);
        Page<ProductEntity> pageable = productsRepository.findAll(PageRequest.of(page, size));
        PageableResponseProducts response = productMapper.createPageableResponseProducts(pageable);
        logger.info("Success attempt to get all products pageable. Request id: {}, total elements: {}, total pages: {}",
                requestId, response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    @Override
    public Long addProduct(ProductRequestDTO product, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to add product. Request id: {}, user agent: {}, product: {}.",
                requestId, userAgent, product.toString());
        CategoryEntity category = categoryService.getCategory(product.getCategoryId(), requestId);
        productsRepository.findBySku(product.getSku())
                .ifPresent(sku -> {
                    throw new BadRequestException("Sku must be unique. Request id: " + requestId);
                });
        ProductEntity productEntity = productsRepository.save(productMapper.createProduct(product, category));
        logger.info("Success product created id: {}, request id: {}", productEntity.getId(), requestId);
        return productEntity.getId();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    @Override
    public void updateProduct(UpdateProductRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to update product. Request id: {}, user agent: {}, product: {}.",
                requestId, userAgent, request.toString());
        ProductEntity product = productsRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with ID:" + request.getId()));
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            productsRepository.findBySku(request.getSku())
                    .ifPresent(sku -> {
                        throw new BadRequestException("Sku must be unique. Request id: " + requestId);
                    });
        }
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryService.getCategory(request.getCategoryId(), requestId);
            productMapper.updateProductCategory(category, product);
        }
        productsRepository.save(productMapper.updateProduct(request, product));
        logger.info("Success product updated productId:{}, request id: {}", product.getId(), requestId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    @Override
    public void changeIsActive(Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to change is active product. Request id: {}, user agent: {}, product id: {}.",
                requestId, userAgent, id);
        ProductEntity product = productsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID:" + id
                        + ". Request id: " + requestId));
        product.setIsActive(!product.getIsActive());
        productsRepository.save(product);
        logger.info("Success change productId: {} active is {}, request id: {}", id, product.getIsActive(), requestId);
    }

    @Override
    public ProductEntity validateAndGetProduct(Long productId, String requestId) {
        ProductEntity product = getProduct(productId, requestId);
        if (!product.getIsActive()) {
            throw new BadRequestException("Product " + product.getName() + " is disabled. Request id: " + requestId);
        }
        if (product.getStockQuantity() == 0) {
            changeIsActive(product, requestId);
            throw new BadRequestException("Product disabled. Product id:" + productId + ". Request id: " + requestId);
        }
        return product;
    }

    @Override
    public ProductEntity getProduct(Long productId, String requestId) {
        return productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with this id don`t exist. ID:" + productId
                        + ". Request id: " + requestId));
    }

    @Override
    public PageableResponseGetProductsByCategory getAllProductByCategoryId(Long categoryId, int page, int size, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to change is active product. Request id: {}, user agent: {}, page: {}, size: {}.",
                requestId, userAgent, page, size);
        Page<ProductEntity> productPage = productsRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
        PageableResponseGetProductsByCategory response = productMapper.createPageableResponseGetProductsByCategory(productPage);
        logger.info("Success get pageable response get all products by categoryId: {}. Request id: {}, Total pages: {}, total elements: {}",
                categoryId, requestId, response.getTotalPages(), response.getTotalPages());
        return response;
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
    public void validateProductsForOrder(List<CartItemEntity> cartItems, String requestId) {
        for (CartItemEntity item : cartItems) {
            ProductEntity product = productsRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product with ID " + item.getProduct().getId()
                            + " not found. Request id: " + requestId));
            if (!product.getIsActive()) {
                throw new BadRequestException("Product " + product.getName()
                        + " is disabled. Request id: " + requestId);
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Not enough '" + product.getName()
                        + "' in stock. Available: " + product.getStockQuantity()
                        + ", requested: " + item.getQuantity()
                        + ". Request id: " + requestId);
            }
        }
    }

    @Transactional
    private void changeIsActive(ProductEntity product, String requestId) {
        product.setIsActive(!product.getIsActive());
        productsRepository.save(product);
        logger.info("Success change is active for productId: {} to {}. Request id: {}",
                product.getId(), product.getIsActive(), requestId);
    }
}
