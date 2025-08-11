package com.example.demo3.api.cart;

import com.example.demo3.api.ApiTest;
import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;
import com.example.demo3.dto.ProductRequestDTO;
import com.example.demo3.dto.CategoryCreateRequestDTO;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.example.demo3.api.assertions.StatusCodeAssertions.*;
import static com.example.demo3.api.cart.CartTestConstants.ADD_ITEM_TO_CART_ENDPOINT;
import static com.example.demo3.api.cart.CartTestConstants.CART_ENDPOINT;
import static com.example.demo3.api.testutil.RestAssuredClient.makeAuthorizedRequest;
import static com.example.demo3.api.testutil.RestAssuredClient.makePostRequestWithIdParam;
import static org.assertj.core.api.Assertions.assertThat;

public class CartApiTest extends ApiTest {

    private Long createProductAndGetId() {
        CategoryCreateRequestDTO category = new CategoryCreateRequestDTO();
        category.setName("Test Category " + System.nanoTime());
        Response categoryResponse = makeAuthorizedRequest(getAdminToken(), category, "/categories", Method.POST);
        assertCreated(categoryResponse);
        String categoryLocation = categoryResponse.getHeader("Location");
        Long categoryId = Long.parseLong(categoryLocation.substring(categoryLocation.lastIndexOf('/') + 1));
        
        ProductRequestDTO product = new ProductRequestDTO();
        product.setName("Test Product " + System.nanoTime());
        product.setPrice(new java.math.BigDecimal("9.99"));
        product.setSku("SKU-" + System.nanoTime());
        product.setStockQuantity(100);
        product.setDescription("Test");
        product.setCategoryId(categoryId);
        Response response = makeAuthorizedRequest(getAdminToken(), product, "/products", Method.POST);
        assertCreated(response);
        return Long.parseLong(response.asString().substring(2));
    }

    private Long getValidProductId() {
        return createProductAndGetId();
    }

    @Nested
    @DisplayName("Get cart tests")
    class GetCart {

        private Response makeRequest(String token) {
            return makeAuthorizedRequest(token, CART_ENDPOINT, Method.GET);
        }

        @Test
        @DisplayName("Should return cart and status code 200")
        void getCart_ShouldReturnCartAndStatusCode200() {
            Response response = makeRequest(getUserToken());
            assertNoContent(response);
        }

        @Test
        @DisplayName("Should return 403 if unauthorized")
        void getCart_ShouldReturn403IfUnauthorized() {
            Response response = makeRequest(INVALID_TOKEN);
            assertForbidden(response);
        }
    }

    @Nested
    @DisplayName("Add item to cart tests")
    class AddItemToCart {

        private Response makeRequest(String token, Long id) {
            return makePostRequestWithIdParam(token, ADD_ITEM_TO_CART_ENDPOINT, id);
        }

        @Test
        @DisplayName("Should add item and return 200")
        void addItem_ShouldAddItemAndReturn200() {
            Long productId = getValidProductId();
            Response response = makeRequest(getUserToken(), productId);
            response.then().log().all();
            assertOk(response);
            assertThat(response.getBody().asString()).contains("Item added to cart");
        }

        @Test
        @DisplayName("Should return 404 for invalid productId")
        void addItem_ShouldReturn404ForInvalidProductId() {
            Long invalidProductId = Long.MAX_VALUE;
            Response response = makeRequest(getUserToken(), invalidProductId);
            assertNotFound(response);
        }

        @Test
        @DisplayName("Should return 401 if unauthorized")
        void addItem_ShouldReturn401IfUnauthorized() {
            Long productId = getValidProductId();
            Response response = makeRequest(INVALID_TOKEN, productId);
            assertUnauthorized(response);
        }
    }

    @Nested
    @DisplayName("Update cart item tests")
    class UpdateCartItem {

        private String getEndpoint(Long productId) {
            return CART_ENDPOINT + "/items/" + productId;
        }

        @Test
        @DisplayName("Should update quantity and return 200")
        void updateItem_ShouldUpdateQuantityAndReturn200() {
            Long productId = getValidProductId();
            makeAuthorizedRequest(getUserToken(), null, getEndpoint(productId), Method.POST);
            UpdateCartItemRequestDTO update = new UpdateCartItemRequestDTO(3);
            Response response = makeAuthorizedRequest(getUserToken(), update, getEndpoint(productId), Method.PUT);
            assertOk(response);
            assertThat(response.getBody().asString()).contains("Cart item updated");
        }

        @Test
        @DisplayName("Should return 404 for invalid productId")
        void updateItem_ShouldReturn404ForInvalidProductId() {
            Long invalidProductId = 999999L;
            UpdateCartItemRequestDTO update = new UpdateCartItemRequestDTO(2);
            Response response = makeAuthorizedRequest(getUserToken(), update, getEndpoint(invalidProductId), Method.PUT);
            assertNotFound(response);
        }

        @Test
        @DisplayName("Should return 400 for invalid quantity")
        void updateItem_ShouldReturn400ForInvalidQuantity() {
            Long productId = getValidProductId();
            UpdateCartItemRequestDTO update = new UpdateCartItemRequestDTO(0); // invalid
            Response response = makeAuthorizedRequest(getUserToken(), update, getEndpoint(productId), Method.PUT);
            assertBadRequest(response);
        }

        @Test
        @DisplayName("Should return 403 if unauthorized")
        void updateItem_ShouldReturn403IfUnauthorized() {
            Long productId = getValidProductId();
            UpdateCartItemRequestDTO update = new UpdateCartItemRequestDTO(2);
            Response response = makeAuthorizedRequest("invalid", update, getEndpoint(productId), Method.PUT);
            assertThat(response.getStatusCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Remove item from cart tests")
    class RemoveItemFromCart {

        private String getEndpoint(Long productId) {
            return CART_ENDPOINT + "/items/" + productId;
        }

        @Test
        @DisplayName("Should remove item and return 200")
        void removeItem_ShouldRemoveItemAndReturn200() {
            Long productId = getValidProductId();
            // Add item first
            makeAuthorizedRequest(getUserToken(), null, getEndpoint(productId), Method.POST);
            Response response = makeAuthorizedRequest(getUserToken(), getEndpoint(productId), Method.DELETE);
            assertOk(response);
            assertThat(response.getBody().asString()).contains("Item removed from cart");
        }

        @Test
        @DisplayName("Should return 404 for invalid productId")
        void removeItem_ShouldReturn404ForInvalidProductId() {
            Long invalidProductId = 999999L;
            Response response = makeAuthorizedRequest(getUserToken(), getEndpoint(invalidProductId), Method.DELETE);
            assertNotFound(response);
        }

        @Test
        @DisplayName("Should return 401 if unauthorized")
        void removeItem_ShouldReturn401IfUnauthorized() {
            Long productId = getValidProductId();
            Response response = makeAuthorizedRequest("invalid", getEndpoint(productId), Method.DELETE);
            assertForbidden(response);
        }

        @Test
        @DisplayName("Should return 403 if unauthorized")
        void removeItem_ShouldReturn403IfUnauthorized() {
            Long productId = getValidProductId();
            Response response = makeAuthorizedRequest("invalid", getEndpoint(productId), Method.DELETE);
            assertThat(response.getStatusCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Clear cart tests")
    class ClearCart {

        @Test
        @DisplayName("Should clear cart and return 200")
        void clearCart_ShouldClearCartAndReturn200() {
            Response response = makeAuthorizedRequest(getUserToken(), CART_ENDPOINT, Method.DELETE);
            assertOk(response);
            assertThat(response.getBody().asString()).contains("Cart cleared");
        }

        @Test
        @DisplayName("Should return 401 if unauthorized")
        void clearCart_ShouldReturn401IfUnauthorized() {
            Response response = makeAuthorizedRequest("invalid", CART_ENDPOINT, Method.DELETE);
            assertForbidden(response);
        }

        @Test
        @DisplayName("Should return 403 if unauthorized")
        void clearCart_ShouldReturn403IfUnauthorized() {
            Response response = makeAuthorizedRequest("invalid", CART_ENDPOINT, Method.DELETE);
            assertThat(response.getStatusCode()).isEqualTo(403);
        }
    }
}
