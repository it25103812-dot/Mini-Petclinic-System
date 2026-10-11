package com.example.clinic;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owners/{ownerId}/pets")
public class PetController {

    private final OwnerRepository owners;
    private final PetRepository pets;
    private final AppointmentRepository appointments;

    public PetController(OwnerRepository owners, PetRepository pets, AppointmentRepository appointments) {
        this.owners = owners;
        this.pets = pets;
        this.appointments = appointments;
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable Integer ownerId) {
        return owners.findById(ownerId).orElseThrow();
    }

    @ModelAttribute("types")
    public List<String> types() {
        return List.of("Dog", "Cat", "Bird", "Hamster", "Rabbit", "Other");
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("pet", new Pet());
        return "petForm";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("owner") Owner owner,
                         @Valid @ModelAttribute("pet") Pet pet, BindingResult result,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "petForm";
        }
        pet.setId(null);
        pet.setOwner(owner);
        pets.save(pet);
        flash.addFlashAttribute("message", "Pet added successfully.");
        return "redirect:/owners/" + owner.getId();
    }

    @GetMapping("/{petId}/edit")
    public String editForm(@PathVariable Integer petId, Model model) {
        model.addAttribute("pet", pets.findById(petId).orElseThrow());
        return "petForm";
    }

    @PostMapping("/{petId}/edit")
    public String update(@PathVariable Integer petId,
                         @ModelAttribute("owner") Owner owner,
                         @Valid @ModelAttribute("pet") Pet form, BindingResult result,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            form.setId(petId);
            return "petForm";
        }
        Pet existing = pets.findById(petId).orElseThrow();
        existing.setName(form.getName());
        existing.setBirthDate(form.getBirthDate());
        existing.setType(form.getType());
        pets.save(existing);
        flash.addFlashAttribute("message", "Pet updated successfully.");
        return "redirect:/owners/" + owner.getId();
    }

    // Medical history of one pet (all visits)
    @GetMapping("/{petId}/history")
    public String history(@PathVariable Integer petId, Model model) {
        model.addAttribute("pet", pets.findById(petId).orElseThrow());
        model.addAttribute("history", appointments.findByPetIdOrderByAppointmentDateDescAppointmentTimeDesc(petId));
        return "petHistory";
    }

    // PERMANENT DELETE - admin only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{petId}/delete")
    public String delete(@PathVariable Integer ownerId, @PathVariable Integer petId,
                         RedirectAttributes flash) {
        pets.deleteById(petId);
        flash.addFlashAttribute("message", "Pet permanently deleted.");
        return "redirect:/owners/" + ownerId;
    }
}
