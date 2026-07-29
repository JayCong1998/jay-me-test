package com.jaymetest.admin;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpLogic;

public final class AdminStpUtil {

    public static final String LOGIN_TYPE = "admin";
    public static final StpLogic STP_LOGIC = new StpLogicJwtForSimple(LOGIN_TYPE);

    private AdminStpUtil() {
    }

    public static void login(Object id) {
        STP_LOGIC.login(id);
    }

    public static void login(Object id, SaLoginModel loginModel) {
        STP_LOGIC.login(id, loginModel);
    }

    public static void logout() {
        STP_LOGIC.logout();
    }

    public static void checkLogin() {
        STP_LOGIC.checkLogin();
    }

    public static long getLoginIdAsLong() {
        return STP_LOGIC.getLoginIdAsLong();
    }

    public static String getTokenValue() {
        return STP_LOGIC.getTokenValue();
    }
}
