package com.jaymetest.service.admin;

import com.jaymetest.admin.AdminStpUtil;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenService {

    public String login(long adminId) {
        AdminStpUtil.login(adminId);
        return AdminStpUtil.getTokenValue();
    }

    public void logout() {
        AdminStpUtil.logout();
    }

    public long getLoginIdAsLong() {
        return AdminStpUtil.getLoginIdAsLong();
    }
}
