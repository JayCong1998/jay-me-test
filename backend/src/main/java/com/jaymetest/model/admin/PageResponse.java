package com.jaymetest.model.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;
}
