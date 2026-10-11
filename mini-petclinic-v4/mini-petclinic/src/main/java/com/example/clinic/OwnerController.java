package com.example.clinic;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OwnerController {

    private final OwnerRepository owners;

    public OwnerController(OwnerRepository owners) {
        this.owners = owners;
    }

    @GetMapping("/")
    public String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("owners", owners.findByLastNameContainingIgnoreCase(search));
        model.addAttribute("search", search);
        return "owners";
    }

    @GetMapping("/owners/{id}")
    public String details(@PathVariable Integer id, Model model) {
        model.addAttribute("owner", owners.findById(id).orElseThrow());
        return "ownerDetails";
    }

    @GetMapping("/owners/new")
    public String newForm(Model model) {
        model.addAttribute("owner", new Owner());
        return "ownerForm";
    }

    @PostMapping("/owners/new")
    public String create(@Valid @ModelAttribute("owner") Owner owner, BindingResult result,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "ownerForm";
        }
        owner.setId(null);
        owners.save(owner);
        flash.addFlashAttribute("message", "Owner added successfully.");
        return "redirect:/owners/" + owner.getId();
    }

    @GetMapping("/owners/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("owner", owners.findById(id).orElseThrow());
        return "ownerForm";
    }

    @PostMapping("/owners/{id}/edit")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("owner") Owner form,
                         BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            form.setId(id);
            return "ownerForm";
        }
        Owner existing = owners.findById(id).orElseThrow();
        existing.setFirstName(form.getFirstName());
        existing.setLastName(form.getLastName());
        existing.setCity(form.getCity());
        existing.setTelephone(form.getTelephone());
        owners.save(existing);
        flash.addFlashAttribute("message", "Owner updated successfully.");
        return "redirect:/owners/" + id;
    }

    // PERMANENT DELETE - admin only (also enforced in SecurityConfig)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/owners/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        owners.deleteById(id);
        flash.addFlashAttribute("message", "Owner permanently deleted.");
        return "redirect:/";
    }
}
