package com.jaymetest.auth;

import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.jwt.exception.SaJwtException;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDisplayNameClaimsTest {

    private static final String JWT_SECRET = "jaymetest2024secretkeyformusic";

    @Test
    void signedUserTokenExposesNicknameOnlyForUserLoginType() {
        String token = jwtLogic("login").createTokenValue(
                42L, "default-device", 3600, Map.of("nickname", "Jay"));

        assertEquals("Jay", SaJwtUtil.getPayloadsNotCheck(token, "login", JWT_SECRET).getStr("nickname"));
        assertThrows(SaJwtException.class,
                () -> SaJwtUtil.getPayloadsNotCheck(token, "admin", JWT_SECRET));
    }

    @Test
    void signedAdminTokenExposesNicknameOnlyForAdminLoginType() {
        String token = jwtLogic("admin").createTokenValue(
                7L, "default-device", 3600, Map.of("nickname", "Operator"));

        assertEquals("Operator", SaJwtUtil.getPayloadsNotCheck(token, "admin", JWT_SECRET).getStr("nickname"));
        assertThrows(SaJwtException.class,
                () -> SaJwtUtil.getPayloadsNotCheck(token, "login", JWT_SECRET));
    }

    private StpLogicJwtForSimple jwtLogic(String loginType) {
        return new StpLogicJwtForSimple(loginType) {
            @Override
            public String jwtSecretKey() {
                return JWT_SECRET;
            }
        };
    }
}
