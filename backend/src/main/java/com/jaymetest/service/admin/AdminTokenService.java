package com.jaymetest.service.admin;

import com.jaymetest.admin.AdminStpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenService {

    public String login(long adminId, String nickname) {
        AdminStpUtil.login(adminId, new SaLoginModel().setExtra("nickname", nickname));
        return AdminStpUtil.getTokenValue();
    }

    public void logout() {
        AdminStpUtil.logout();
    }

    public long getLoginIdAsLong() {
        return AdminStpUtil.getLoginIdAsLong();
    }
}
