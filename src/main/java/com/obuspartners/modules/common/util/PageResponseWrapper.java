package com.obuspartners.modules.common.util;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponseWrapper<T> {
    private boolean status;
    private int statusCode;
    private String message;
    private List<T> data;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponseWrapper<T> fromPage(Page<T> page, String message) {
        return new PageResponseWrapper<>(
                true,
                200,
                message,
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
