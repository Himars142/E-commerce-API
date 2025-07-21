package com.example.demo3.api.category;

import com.example.demo3.api.ApiTest;
import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.example.demo3.api.assertions.Assertions.assertUserIsNotAdmin;
import static com.example.demo3.api.assertions.Assertions.assertValidationError;
import static com.example.demo3.api.assertions.PageableAssertions.*;
import static com.example.demo3.api.assertions.StatusCodeAssertions.assertCreated;
import static com.example.demo3.api.assertions.StatusCodeAssertions.assertOk;
import static com.example.demo3.api.category.CategoryTestFactory.createCategoryCreateRequestDTO;
import static com.example.demo3.api.testutil.RestAssuredClient.makeAuthorizedPostRequest;
import static com.example.demo3.api.testutil.RestAssuredClient.makePageableGetRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CategoryApiTest extends ApiTest {

    private static final String CATEGORIES_ENDPOINT = "/categories";

    @Nested
    @DisplayName("Get all categories tests")
    class GetAllCategory {

        private static final String PAGE_IS_NEGATIVE_ERROR_MESSAGE = "має бути більше або рівне 0";
        private static final String SIZE_EXCEED_MAX_LIMIT_ERROR_MESSAGE = "має бути менше або рівне 50";
        private static final String SIZE_IS_NEGATIVE_OR_ZERO_ERROR_MESSAGE = "має бути більше або рівне 1";

        private Response makeRequest(Integer page, Integer size) {
            return makePageableGetRequest(CATEGORIES_ENDPOINT, page, size);
        }

        @Test
        @DisplayName("Should return default pagination when no parameters provided")
        void getAllCategories_NoParams_ShouldReturnDefaultPagination() {
            Response response = makeRequest(null, null);

            assertOk(response);

            PageableResponseCategoryDTO pageableResponse = response.as(PageableResponseCategoryDTO.class);
            assertFirstPage(pageableResponse, DEFAULT_SIZE);
        }

        @Test
        @DisplayName("Should return first page when page is 0")
        void getAllCategories_WhenPageZero_ShouldReturnFirstPage() {
            Response response = makeRequest(0, DEFAULT_SIZE);

            assertOk(response);

            PageableResponseCategoryDTO pageableResponse = response.as(PageableResponseCategoryDTO.class);

            assertFirstPage(pageableResponse, DEFAULT_SIZE);
        }

        @ParameterizedTest
        @CsvSource({
                "1, 5",
                "1, 10",
                "2, 20"
        })
        @DisplayName("Should return correct pagination for valid page and size combinations")
        void getAllCategories_WhenPage0AndSize10_ShouldReturnCodeAndValidResponse(int page, int size) {
            Response response = makeRequest(page, size);

            assertOk(response);

            PageableResponseCategoryDTO pageableResponse = response.as(PageableResponseCategoryDTO.class);

            assertNotFirstPage(pageableResponse, page, size);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("When page is negative should return code 400 and return error")
        void getAllCategories_NegativePage_ShouldReturn400(int negativePage) {
            Response response = makeRequest(negativePage, DEFAULT_SIZE);

            assertValidationError(response, "page", PAGE_IS_NEGATIVE_ERROR_MESSAGE);
        }

        @ParameterizedTest
        @ValueSource(ints = {51, 100, 1000})
        @DisplayName("Should return 400 when size exceeds maximum allowed")
        void getAllCategories_SizeExceedsLimit_ShouldReturn400(int invalidSize) {
            Response response = makeRequest(DEFAULT_PAGE, invalidSize);

            assertValidationError(response, "size", SIZE_EXCEED_MAX_LIMIT_ERROR_MESSAGE);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -10, -100})
        @DisplayName("Should return 400 when size negative or zero")
        void getAllCategories_NegativeSize_ShouldReturn400(int invalidSize) {
            Response response = makeRequest(DEFAULT_PAGE, invalidSize);

            assertValidationError(response, "size", SIZE_IS_NEGATIVE_OR_ZERO_ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Should return 400 with multiple validation errors")
        void getAllCategories_MultipleValidationErrors_ShouldReturn400() {
            Response response = makeRequest(-1, MAX_ALLOWED_SIZE + 1);

            response.then().statusCode(400);

            String responseBody = response.asString();
            assertThat(responseBody).contains("page");
            assertThat(responseBody).contains(PAGE_IS_NEGATIVE_ERROR_MESSAGE);
            assertThat(responseBody).contains("size");
            assertThat(responseBody).contains(SIZE_EXCEED_MAX_LIMIT_ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Should handle edge case: page beyond available data")
        void getAllCategories_PageBeyondData_ShouldReturnEmptyPage() {
            int veryHighPage = 9999;

            Response response = makeRequest(veryHighPage, DEFAULT_SIZE);

            assertOk(response);

            PageableResponseCategoryDTO pageableResponse = response.as(PageableResponseCategoryDTO.class);

            assertPageableStructure(pageableResponse);
            assertThat(pageableResponse.getPageNumber()).isEqualTo(veryHighPage);
            assertThat(pageableResponse.getPageSize()).isEqualTo(DEFAULT_SIZE);
        }
    }

    @Nested
    @DisplayName("Create category tests")
    class CreateCategory {

        public static final String CATEGORY_CREATED_MESSAGE = "Category created";
        private static final String NAME_IS_BLANK_OR_NULL_ERROR_MESSAGE = "Category name cannot be blank or null";

        private Response makeRequest(String token, CategoryCreateRequestDTO request) {
            return makeAuthorizedPostRequest(token, request, CATEGORIES_ENDPOINT);
        }

        @Test
        @DisplayName("Should return status code 200 and create category")
        void createCategory_ValidData_ShouldReturn201() {
            CategoryCreateRequestDTO request = CategoryTestFactory.createCategoryCreateRequestDTO();

            Response response = makeRequest(getAdminToken(), request);

            assertCreated(response);
            assertThat(response.asString()).isEqualTo(CATEGORY_CREATED_MESSAGE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should return 400 when name is blank or whitespace")
        void createCategory_BlankName_ShouldReturn400(String blankName) {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO(blankName);

            Response response = makeRequest(getAdminToken(), request);

            assertValidationError(response, "name", NAME_IS_BLANK_OR_NULL_ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void createCategory_NullName_ShouldReturn400() {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO(null);

            Response response = makeRequest(getAdminToken(), request);

            assertValidationError(response, "name", NAME_IS_BLANK_OR_NULL_ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void createCategory_NonAdminUser_ShouldReturn403() {
            CategoryCreateRequestDTO request = CategoryTestFactory.createCategoryCreateRequestDTO();

            Response response = makeRequest(getUserToken(), request);

            assertUserIsNotAdmin(response);
        }
    }
}