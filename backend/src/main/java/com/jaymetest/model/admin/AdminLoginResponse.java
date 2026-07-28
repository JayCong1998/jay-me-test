package com.jaymetest.model.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {
    private String token;
    private AdminDTO admin;
}
