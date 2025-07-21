package com.example.demo3.api.assertions;

import com.example.demo3.dto.PageableResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PageableAssertions {

    public static <T> void assertPageableStructure(PageableResponse<T> response) {
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNotNull();
        assertThat(response.getPageNumber()).isNotNull().isNotNegative();
        assertThat(response.getPageSize()).isNotNull().isNotNegative();
        assertThat(response.getTotalPages()).isNotNull().isNotNegative();
        assertThat(response.getTotalElements()).isNotNull().isNotNegative();
    }

    public static <T> void assertPageableValues(PageableResponse<T> response,
                                                int expectedPage,
                                                int expectedSize) {
        assertPageableStructure(response);
        assertThat(response.getPageNumber()).isEqualTo(expectedPage);
        assertThat(response.getPageSize()).isEqualTo(expectedSize);
    }

    public static <T> void assertFirstPage(PageableResponse<T> response, int expectedSize) {
        assertPageableValues(response, 0, expectedSize);
        assertThat(response.isFirst()).isTrue();

        if (response.getTotalPages() <= 1) {
            assertThat(response.isLast()).isTrue();
        } else {
            assertThat(response.isLast()).isFalse();
        }
    }

    public static <T> void assertNotFirstPage(PageableResponse<T> response,
                                              int expectedPage,
                                              int expectedSize) {
        assertPageableValues(response, expectedPage, expectedSize);
        assertThat(response.isFirst()).isFalse();

        assertThat(response.isLast()).isIn(true, false);
    }
}
