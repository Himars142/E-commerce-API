package com.example.demo3.api.category;

import com.example.demo3.api.ApiTest;
import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.CategoryDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.example.demo3.api.assertions.Assertions.*;
import static com.example.demo3.api.assertions.PageableAssertions.*;
import static com.example.demo3.api.assertions.StatusCodeAssertions.*;
import static com.example.demo3.api.category.CategoryTestConstants.*;
import static com.example.demo3.api.category.CategoryTestFactory.createCategoryCreateRequestDTO;
import static com.example.demo3.api.testutil.RestAssuredClient.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CategoryApiTest extends ApiTest {

    @Nested
    @DisplayName("Get all categories tests")
    class GetAllCategory {

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
        @DisplayName("Should return first page")
        void getAllCategories_WhenFirstPage_ShouldReturnFirstPage() {
            Response response = makeRequest(FIRST_PAGE, DEFAULT_SIZE);

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

            assertValidationError(response, "page");
        }

        @ParameterizedTest
        @ValueSource(ints = {51, 100, 1000})
        @DisplayName("Should return 400 when size exceeds maximum allowed")
        void getAllCategories_SizeExceedsLimit_ShouldReturn400(int invalidSize) {
            Response response = makeRequest(DEFAULT_PAGE, invalidSize);

            assertValidationError(response, "size");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -10, -100})
        @DisplayName("Should return 400 when size negative or zero")
        void getAllCategories_NegativeSize_ShouldReturn400(int invalidSize) {
            Response response = makeRequest(DEFAULT_PAGE, invalidSize);

            assertValidationError(response, "size");
        }

        @Test
        @DisplayName("Should return 400 with multiple validation errors")
        void getAllCategories_MultipleValidationErrors_ShouldReturn400() {
            Response response = makeRequest(-1, MAX_ALLOWED_PAGE_SIZE + 1);

            assertBadRequest(response);

            String responseBody = response.asString();
            assertThat(responseBody).contains("page");
            assertThat(responseBody).contains("size");
        }

        @Test
        @DisplayName("Should handle edge case: page beyond available data")
        void getAllCategories_PageBeyondData_ShouldReturnEmptyPage() {
            Response response = makeRequest(PAGE_BEYOND_DATA, DEFAULT_SIZE);

            assertOk(response);

            PageableResponseCategoryDTO pageableResponse = response.as(PageableResponseCategoryDTO.class);

            assertPageableStructure(pageableResponse);
            assertThat(pageableResponse.getContent().isEmpty()).isTrue();
            assertThat(pageableResponse.isLast()).isTrue();
            assertThat(pageableResponse.getPageNumber()).isEqualTo(PAGE_BEYOND_DATA);
            assertThat(pageableResponse.getPageSize()).isEqualTo(DEFAULT_SIZE);
        }
    }

    @Nested
    @DisplayName("Create category tests")
    class CreateCategory {

        private Response makeRequest(String token, CategoryCreateRequestDTO request) {
            return makeAuthorizedPostRequest(token, request, CATEGORIES_ENDPOINT);
        }

        @Test
        @DisplayName("Should throw status code 400")
        void createCategory_NoAuthorizationHeader_ShouldThrowStatusCode401() {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO();

            Response response = makeRequestWithoutAuthorizationHeader(request, CATEGORIES_ENDPOINT, Method.POST);

            assertUnauthorized(response);
        }

        @Test
        @DisplayName("Should return status code 201 and create category")
        void createCategory_ValidData_ShouldReturn201() {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO();

            Response response = makeRequest(getAdminToken(), request);

            assertCreated(response);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should return 400 when name is blank or whitespace")
        void createCategory_BlankName_ShouldReturn400(String blankName) {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO(blankName);

            Response response = makeRequest(getAdminToken(), request);

            assertValidationError(response, "name");
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void createCategory_NullName_ShouldReturn400() {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO(null);

            Response response = makeRequest(getAdminToken(), request);

            assertValidationError(response, "name");
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void createCategory_NonAdminUser_ShouldReturn403() {
            CategoryCreateRequestDTO request = createCategoryCreateRequestDTO();

            Response response = makeRequest(getUserToken(), request);

            assertUserIsNotAdmin(response);
        }
    }

    @Nested
    @DisplayName("Get category by id tests")
    class GetCategoryById {

        private Response makeRequest(Long id) {
            return makeGetRequestWithIdParam(CATEGORIES_ENDPOINT, id);
        }

        @Test
        @DisplayName("When id not positive should throw status code 400")
        void getCategoryById_IdNotPositive_ShouldThrowStatusCode400() {
            Response response = makeRequest(INVALID_NOT_POSITIVE_CATEGORY_ID);

            assertBadRequest(response);
            assertValidationError(response, "id");
        }

        @Test
        @DisplayName("When valid id should return category")
        void getCategoryById_ShouldReturnCategory() {
            Long id = createCategoryAndGetId();
            Response response = makeRequest(id);

            CategoryDTO categoryDTO = response.as(CategoryDTO.class);

            assertOk(response);
            assertThat(categoryDTO.getId()).isEqualTo(id);

            deleteCategoryById(id);
        }

        @Test
        @DisplayName("When category do not exist should throw not found")
        void getCategoryById_CategoryDoNotExist_ShouldThrowNotFound() {
            Response response = makeRequest(Long.MAX_VALUE);

            assertNotFound(response);
            assertMessageHasRequestId(response);
        }
    }

    private static void deleteCategoryById(Long id) {
        makeDeleteRequestWithIdParam(getAdminToken(), CATEGORIES_ENDPOINT, id);
    }

    @Nested
    @DisplayName("Delete category by id tests")
    class DeleteCategoryById {

        private Response makeRequest(String token, Long id) {
            return makeDeleteRequestWithIdParam(token, CATEGORIES_ENDPOINT, id);
        }

        @Test
        @DisplayName("When id not positive should throw status code 400")
        void deleteCategoryById_IdNotPositive_ShouldThrowStatusCode400() {
            Response response = makeRequest(getAdminToken(), INVALID_NOT_POSITIVE_CATEGORY_ID);

            assertBadRequest(response);
            assertValidationError(response, "id");
        }

        @Test
        @DisplayName("When valid id should delete category")
        void getCategoryById_ShouldReturnCategory() {
            Long id = createCategoryAndGetId();

            Response deleteResponse = makeRequest(getAdminToken(), id);

            assertOk(deleteResponse);

            Response getResponse = makeGetRequestWithIdParam(CATEGORIES_ENDPOINT, id);

            assertNotFound(getResponse);
            assertMessageHasRequestId(getResponse);
        }

        @Test
        @DisplayName("When category do not exist should throw not found")
        void deleteCategoryById_CategoryDoNotExist_ShouldThrowNotFound() {
            Response response = makeRequest(getAdminToken(), Long.MAX_VALUE);

            assertNotFound(response);
            assertMessageHasRequestId(response);
        }

        @Test
        @DisplayName("When user not admin should return status code 403 and throw forbidden exception")
        void deleteCategoryId_UserNotAdmin_ShouldReturn403() {
            Response response = makeRequest(getUserToken(), VALID_CATEGORY_ID);

            assertForbidden(response);
        }
    }

    private static Long createCategoryAndGetId() {
        CategoryCreateRequestDTO request = createCategoryCreateRequestDTO();
        Response createCategoryResponse = makeAuthorizedPostRequest(getAdminToken(), request, CATEGORIES_ENDPOINT);
        String URI = createCategoryResponse.getHeader("Location");
        return Long.parseLong(URI.substring(URI.lastIndexOf('/') + 1));
    }
}