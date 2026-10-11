package com.example.clinic;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/doctors")
public class AdminController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AdminController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", users.findAllByOrderByRoleAscFullNameAsc());
        return "doctors";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new UserForm());
        return "userForm";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") UserForm form, BindingResult result,
                         RedirectAttributes flash) {
        if (form.getUsername() != null && users.existsByUsername(form.getUsername().trim())) {
            result.rejectValue("username", "dup", "Username already taken");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            result.rejectValue("password", "len", "Password must be at least 6 characters");
        }
        if (result.hasErrors()) {
            return "userForm";
        }
        AppUser u = new AppUser();
        u.setUsername(form.getUsername().trim());
        u.setFullName(form.getFullName().trim());
        u.setSpecialization(form.getSpecialization());
        u.setPassword(encoder.encode(form.getPassword()));
        u.setRole("DOCTOR");
        u.setEnabled(true);
        users.save(u);
        flash.addFlashAttribute("message", "Doctor account created.");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        AppUser u = users.findById(id).orElseThrow();
        UserForm f = new UserForm();
        f.setId(u.getId());
        f.setUsername(u.getUsername());
        f.setFullName(u.getFullName());
        f.setSpecialization(u.getSpecialization());
        f.setEnabled(u.isEnabled());
        model.addAttribute("form", f);
        return "userForm";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("form") UserForm form,
                         BindingResult result, Authentication auth, RedirectAttributes flash) {
        boolean newPassword = form.getPassword() != null && !form.getPassword().isBlank();
        if (newPassword && form.getPassword().length() < 6) {
            result.rejectValue("password", "len", "Password must be at least 6 characters");
        }
        if (result.hasErrors()) {
            form.setId(id);
            return "userForm";
        }
        AppUser u = users.findById(id).orElseThrow();
        u.setFullName(form.getFullName().trim());
        u.setSpecialization(form.getSpecialization());
        if (newPassword) {
            u.setPassword(encoder.encode(form.getPassword()));
        }
        // an admin cannot lock themselves out
        if (!u.getUsername().equals(auth.getName())) {
            u.setEnabled(form.isEnabled());
        }
        users.save(u);
        flash.addFlashAttribute("message", "Account updated.");
        return "redirect:/admin/doctors";
    }
}
