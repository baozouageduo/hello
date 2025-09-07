package com.tests.campuslostandfoundsystem.controller;

import com.tests.campuslostandfoundsystem.entity.R;
import com.tests.campuslostandfoundsystem.entity.auth.LoginInfoDTO;
import com.tests.campuslostandfoundsystem.entity.auth.LoginSuccessDTO;
import com.tests.campuslostandfoundsystem.entity.auth.RefreshTokenInfoDTO;
import com.tests.campuslostandfoundsystem.entity.auth.RefreshTokenSuccessDTO;
import com.tests.campuslostandfoundsystem.entity.utils.GraphCaptcha;
import com.tests.campuslostandfoundsystem.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录（用户名 + 密码 + 图形验证码）")
    @PostMapping("/login")
    public R<LoginSuccessDTO> login(@RequestBody LoginInfoDTO dto) {
        return R.success(authService.login(dto));
    }

    @Operation(summary = "刷新 accessToken")
    @PostMapping("/refreshToken")
    public R<RefreshTokenSuccessDTO> refresh(@RequestBody RefreshTokenInfoDTO dto) {
        return R.success(authService.refreshToken(dto));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout(@RequestBody RefreshTokenInfoDTO dto) {
        authService.logout(dto);
        return R.success(null);
    }

    @Operation(summary = "获取图形验证码（JSON，包含 captchaKey 和 captchaImage[dataURL]）")
    @GetMapping("/GraphCaptcha")
    public R<GraphCaptcha> graphCaptcha() {
        GraphCaptcha gc = authService.generateCaptcha();
        // 出于安全，后端已将 captchaCode 清空，仅返回 key 和 image
        return R.success(gc);
    }
}
