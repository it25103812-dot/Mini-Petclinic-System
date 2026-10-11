package com.example.clinic;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the logged-in user available to every template as "me". */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AppUserRepository users;

    public GlobalModelAdvice(AppUserRepository users) {
        this.users = users;
    }

    @ModelAttribute("me")
    public AppUser me(Principal principal) {
        return principal == null ? null : users.findByUsername(principal.getName()).orElse(null);
    }
}
