package com.jaymetest.model.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDTO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
}
