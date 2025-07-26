# E-commerce API

This project is a RESTful API for an e-commerce platform built with Spring Boot. The API allows you to manage users, products, categories, cart, and orders, and includes authentication and authorization mechanisms based on JWT.

## API Endpoints

### Authentication & Users

- `POST /api/users/register`  
  Register a new user.

- `POST /api/users/login`  
  Login and receive JWT tokens.

- `GET /api/users/`  
  Get current user profile (JWT required).

- `PUT /api/users/`  
  Update current user profile (JWT required).

---

### Products

- `GET /api/products`  
  Get all products (pagination: `page`, `size`).

- `GET /api/products/{id}`  
  Get product by ID.

- `POST /api/products`  
  Add a new product (ADMIN, JWT required).

- `PUT /api/products`  
  Update a product (ADMIN, JWT required).

- `PUT /api/products/{id}`  
  Toggle product active status (ADMIN, JWT required).

- `GET /api/products/category/{categoryId}`  
  Get products by category (pagination: `page`, `size`).

---

### Categories

- `GET /api/categories`  
  Get all categories (pagination: `page`, `size`).

- `GET /api/categories/{id}`  
  Get category by ID.

- `POST /api/categories`  
  Create a new category (ADMIN, JWT required).

- `DELETE /api/categories/{id}`  
  Delete category by ID (ADMIN, JWT required).

---

### Cart

- `GET /api/cart`  
  Get current user's cart (JWT required).

- `POST /api/cart/items/{productId}`  
  Add product to cart (JWT required).

- `PUT /api/cart/items/{productId}`  
  Update quantity of a product in cart (JWT required).

- `DELETE /api/cart/items/{productId}`  
  Remove product from cart (JWT required).

---

### Orders

- `GET /api/orders`  
  Get current user's orders (JWT required).

- `POST /api/orders`  
  Create a new order from cart (JWT required).

- `GET /api/orders/{id}`  
  Get order by ID (JWT required).

- `PUT /api/orders/{id}/cancel`  
  Cancel order by ID (JWT required).

**Note:**  
- All endpoints requiring authentication expect the `Authorization: Bearer <token>` header.
- Pagination parameters: `page` (default 0), `size` (default 10, max 50).

---

## Admin Endpoints

> All admin endpoints require an `Authorization: Bearer <token>` header with an **admin** user's JWT.

### Category Management

- `POST /api/categories`
  - Create a new category.
  - **Body:** `CategoryCreateRequestDTO`
- `DELETE /api/categories/{id}`
  - Delete a category by ID.

---

### Product Management

- `POST /api/products`
  - Add a new product.
  - **Body:** `ProductRequestDTO`
- `PUT /api/products`
  - Update a product.
  - **Body:** `UpdateProductRequestDTO`
- `PUT /api/products/{id}`
  - Toggle product active status (enable/disable).

---

### Order Management

- `GET /api/orders/`
  - Get all orders (with optional status filter, pagination).
  - **Query:** `page`, `size`, `status`
- `PUT /api/orders/{orderId}/status`
  - Update order status (e.g., approve, ship, complete, cancel).
  - **Body:** `UpdateOrderStatusRequestDTO`

**Note:**  
- All admin endpoints are protected and will return `403 Forbidden` if the user is not an admin.
- For full request/response details, see the DTOs in the `dto` package.

---

## Project Structure

```
src/
  main/
    java/com/example/demo3/
      controller/    # REST controllers (User, Product, Category, Cart, Order)
      dto/           # DTOs for requests/responses
      entity/        # JPA entities (User, Product, Category, Cart, Order)
      exception/     # Custom exceptions and error handler
      mapper/        # Entity-DTO mappers
      repository/    # Spring Data JPA repositories
      security/      # JWT, SecurityConfig
      service/       # Business logic
      utill/         # Utilities
    resources/
      application.properties
      log4j2-spring.xml
      database/initDB.sql
  test/
    java/com/example/demo3/
      api/           # API integration tests
      repository/    # Repository tests
      service/       # Service unit tests
```
---

## Running the Project

### Requirements

- Java 17+
- Maven 3.9+
- PostgreSQL

### Setup Steps

1. **Clone the repository:**
   ```sh
   git clone https://github.com/Himars142/E-commerce-API.git
   cd demo3
   ```

2. **Configure the database:**
   - Set your PostgreSQL credentials in `src/main/resources/application.properties`:
     ```
     spring.datasource.url=your_database_url
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Initialize the DB schema:**
   - SQL script for creating tables: `src/main/resources/database/initDB.sql`

4. **Run the application:**
   ```sh
./mvnw spring-boot:run
   ```
   The API will be available at: `http://localhost:9999/api`

## Testing

Run all tests:
```sh
./mvnw test
```

## Configuration

- **Logging:** [log4j2-spring.xml](src/main/resources/log4j2-spring.xml)
- **DB Schema:** [initDB.sql](src/main/resources/database/initDB.sql)
- **JWT Settings:** in application.properties
