package com.example.api.security.pwdreset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenPasswordResetServiceTest {

    @Autowired
    TokenPasswordResetService tokenPasswordResetService;

    @Test
    void isTokenExpired() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(tokenPasswordResetService.isTokenExpired(calendar.getTime()));
    }
}