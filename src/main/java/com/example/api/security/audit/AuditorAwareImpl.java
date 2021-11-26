package com.example.api.security.audit;

import com.example.api.security.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @author Oleg Turchin
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Optional<String> auditor;
        String userName;
        if (null == SecurityContextHolder.getContext().getAuthentication()) {
            userName = "System";
        } else {
            userName = ((UserDetailsImpl)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        }
        auditor = Optional.of(userName);
        return auditor;
    }
}
