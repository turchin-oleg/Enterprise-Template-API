package com.example.api.services;

import com.example.api.user.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Service for running actions after launching the application
 *   Check the users table - if there are no entries add the admin user
 * @author Oleg Turchin
 */
@Component
@Log4j2
public class AfterStartService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AfterStartService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        checkUsersTable();
    }

    public void checkUsersTable () {
        if (userRepository.count() == 0) {
            log.info("There are no records in the users table, I add an admin user with default settings.");
            User userAdmin = new User();
            userAdmin.setLogin("admin");
            userAdmin.setPassword(passwordEncoder.encode("admin"));
            userAdmin.setFullName("admin_test");
            userAdmin.setEmail("admin@example.com");
            userAdmin.setUserRole(UserRole.ROLE_ADMIN);
            userAdmin.setLang(UserLang.EN);
            userAdmin.setTheme(UserTheme.AUTO);
            userAdmin.setActive(true);
            userAdmin.setTokenExp(86400000L);
            // fixme
            userAdmin.setUrl_avatar("");
            userAdmin = userRepository.save(userAdmin);
            if (userAdmin.getId() == null) {
                log.error("Failed to add admin entry to users table. Check your database connection settings.");
            } else {
                log.warn("\t\t Added admin user to users table. Change the password for the admin user!");
            }
        }
    }
}
