package com.college.yi.ecsite.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.college.yi.ecsite.admin.repository.UserMapper;
import com.college.yi.ecsite.entity.User;
import com.college.yi.ecsite.exception.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    @InjectMocks
    private AdminLoginService adminLoginService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 正常系：ログイン成功
    @Test
    void testAuthenticate_success() {
        String inputEmail = "admin@example.com";
        String inputPassword = "password123";
        String dbPasswordHash = "$2a$10$abcdef";

        User user = new User();
        user.setEmail(inputEmail);
        user.setPassword_hash(dbPasswordHash);
        user.setRole(1);

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(user);
        when(passwordEncoder.matches(inputPassword, dbPasswordHash)).thenReturn(true);

        User result = adminLoginService.authenticator(inputEmail, inputPassword);

        assertNotNull(result);
        assertEquals("admin@example.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
    }

    // 異常系：ユーザーが存在しない
    @Test
    void testAuthenticate_userNotFound() {
        String inputEmail = "unknown@example.com";
        String inputPassword = "password123";

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(null);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("ユーザーが存在しません。", ex.getMessage());
    }

    // 異常系：パスワード不一致
    @Test
    void testAuthenticate_wrongPassword() {
        String inputEmail = "admin@example.com";
        String inputPassword = "wrongpass";
        String dbPasswordHash = "$2a$10$abcdef";

        User user = new User();
        user.setEmail(inputEmail);
        user.setPassword_hash(dbPasswordHash);
        user.setRole(1);

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(user);
        when(passwordEncoder.matches(inputPassword, dbPasswordHash)).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("パスワードが一致しません。", ex.getMessage());
    }

    // 異常系：管理者権限がない
    @Test
    void testAuthenticate_notAdmin() {
        String inputEmail = "user@example.com";
        String inputPassword = "password123";
        String dbPasswordHash = "$2a$10$abcdef";

        User user = new User();
        user.setEmail(inputEmail);
        user.setPassword_hash(dbPasswordHash);
        user.setRole(0);

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(user);
        when(passwordEncoder.matches(inputPassword, dbPasswordHash)).thenReturn(true);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("管理者権限がありません", ex.getMessage());
    }

    // 境界値：メールアドレスが50文字のときに認証成功
    @Test
    void testAuthenticate_emailLength50_success() {
        String inputEmail = "a".repeat(50);
        String inputPassword = "password123";
        String dbPasswordHash = "$2a$10$abcdef";

        User user = new User();
        user.setEmail(inputEmail);
        user.setPassword_hash(dbPasswordHash);
        user.setRole(1);

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(user);
        when(passwordEncoder.matches(inputPassword, dbPasswordHash)).thenReturn(true);

        User result = adminLoginService.authenticator(inputEmail, inputPassword);
        assertNotNull(result);
        assertEquals(50, result.getEmail().length());
    }

    // Nullチェック：メールがnull
    @Test
    void testAuthenticate_emailNull() {
        String inputEmail = null;
        String inputPassword = "password123";

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("ユーザーが存在しません。", ex.getMessage());
    }

    // Nullチェック：パスワードがnull
    @Test
    void testAuthenticate_passwordNull() {
        String inputEmail = "admin@example.com";
        String inputPassword = null;
        String dbPasswordHash = "$2a$10$abcdef";

        User user = new User();
        user.setEmail(inputEmail);
        user.setPassword_hash(dbPasswordHash);
        user.setRole(1);

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(user);
        when(passwordEncoder.matches(null, dbPasswordHash)).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("パスワードが一致しません。", ex.getMessage());
    }

    // 依存先がnullを返したとき
    @Test
    void testAuthenticate_userMapperReturnsNull() {
        String inputEmail = "noadmin@example.com";
        String inputPassword = "password123";

        when(userMapper.findAdminByEmail(inputEmail)).thenReturn(null);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            adminLoginService.authenticator(inputEmail, inputPassword)
        );

        assertEquals("ユーザーが存在しません。", ex.getMessage());
    }
}