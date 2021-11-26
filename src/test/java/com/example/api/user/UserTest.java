package com.example.api.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void testAddUser() {
        User user = new User();
        user.setLogin("admin_test");
        user.setPassword("123");
        user.setFullName("admin_test");
        user.setEmail("turchin.om3@gmail.com");
        user.setUserRole(UserRole.ROLE_ADMIN);
        user.setLang(UserLang.EN);
        user.setTheme(UserTheme.AUTO);
        user.setActive(true);
        user.setTokenExp(86400000L);
        user.setUrl_avatar("");
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        userRepository.delete(savedUser);
    }
}