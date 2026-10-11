package com.example.clinic;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository appointments;
    private final PetRepository pets;
    private final AppUserRepository users;

    public AppointmentController(AppointmentRepository appointments, PetRepository pets,
                                 AppUserRepository users) {
        this.appointments = appointments;
        this.pets = pets;
        this.users = users;
    }

    @ModelAttribute("pets")
    public List<Pet> allPets() {
        return pets.findAll(Sort.by("name"));
    }

    @ModelAttribute("doctors")
    public List<AppUser> doctors() {
        return users.findByRoleAndEnabledTrueOrderByFullNameAsc("DOCTOR");
    }

    // ---------- CREATE ----------
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) Integer petId,
                          Authentication auth, Model model) {
        AppUser me = users.findByUsername(auth.getName()).orElseThrow();
        Appointment a = new Appointment();
        a.setAppointmentDate(date != null ? date : LocalDate.now());
        a.setAppointmentTime(LocalTime.of(9, 0));
        model.addAttribute("appointment", a);
        model.addAttribute("selectedPetId", petId);
        model.addAttribute("selectedDoctorId", "DOCTOR".equals(me.getRole()) ? me.getId() : null);
        return "appointmentForm";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("appointment") Appointment appt,
                         BindingResult result,
                         @RequestParam(required = false) Integer petId,
                         @RequestParam(required = false) Integer doctorId,
                         Model model, RedirectAttributes flash) {
        attachPetAndDoctor(appt, petId, doctorId, result);
        checkConflict(appt, null, result);
        if (result.hasErrors()) {
            model.addAttribute("selectedPetId", petId);
            model.addAttribute("selectedDoctorId", doctorId);
            return "appointmentForm";
        }
        appt.setId(null);
        appt.setStatus("PENDING");
        appt.setDiagnosis(null);
        appt.setTreatment(null);
        appt.setNotes(null);
        appointments.save(appt);
        flash.addFlashAttribute("message", "Booking added successfully.");
        return redirectToMonth(appt.getAppointmentDate());
    }

    // ---------- UPDATE ----------
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Appointment a = appointments.findById(id).orElseThrow();
        model.addAttribute("appointment", a);
        model.addAttribute("selectedPetId", a.getPet().getId());
        model.addAttribute("selectedDoctorId", a.getDoctor().getId());
        return "appointmentForm";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("appointment") Appointment form,
                         BindingResult result,
                         @RequestParam(required = false) Integer petId,
                         @RequestParam(required = false) Integer doctorId,
                         Model model, RedirectAttributes flash) {
        attachPetAndDoctor(form, petId, doctorId, result);
        checkConflict(form, id, result);
        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("selectedPetId", petId);
            model.addAttribute("selectedDoctorId", doctorId);
            return "appointmentForm";
        }
        Appointment existing = appointments.findById(id).orElseThrow();
        existing.setAppointmentDate(form.getAppointmentDate());
        existing.setAppointmentTime(form.getAppointmentTime());
        existing.setReason(form.getReason());
        existing.setPet(form.getPet());
        existing.setDoctor(form.getDoctor());
        appointments.save(existing);
        flash.addFlashAttribute("message", "Booking updated successfully.");
        return redirectToMonth(existing.getAppointmentDate());
    }

    // ---------- STATUS (cancel / re-open) - doctors can use this instead of deleting ----------
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Integer id, @RequestParam String status,
                               @RequestParam(defaultValue = "calendar") String from,
                               RedirectAttributes flash) {
        Appointment a = appointments.findById(id).orElseThrow();
        if (!"CANCELLED".equals(status) && !"PENDING".equals(status)) {
            flash.addFlashAttribute("message", "Invalid status.");
        } else if ("COMPLETED".equals(a.getStatus())) {
            flash.addFlashAttribute("message", "A completed visit cannot be changed.");
        } else {
            a.setStatus(status);
            appointments.save(a);
            flash.addFlashAttribute("message",
                    "CANCELLED".equals(status) ? "Booking cancelled." : "Booking re-opened.");
        }
        return "dashboard".equals(from) ? "redirect:/dashboard" : redirectToMonth(a.getAppointmentDate());
    }

    // ---------- VISIT RECORD (doctor side) ----------
    @GetMapping("/{id}/visit")
    public String visitForm(@PathVariable Integer id, Authentication auth, Model model) {
        Appointment a = appointments.findById(id).orElseThrow();
        checkCanTreat(a, auth);
        model.addAttribute("appointment", a);
        model.addAttribute("diagnosis", a.getDiagnosis());
        model.addAttribute("treatment", a.getTreatment());
        model.addAttribute("notes", a.getNotes());
        return "visitForm";
    }

    @PostMapping("/{id}/visit")
    public String saveVisit(@PathVariable Integer id,
                            @RequestParam(defaultValue = "") String diagnosis,
                            @RequestParam(required = false) String treatment,
                            @RequestParam(required = false) String notes,
                            Authentication auth, Model model, RedirectAttributes flash) {
        Appointment a = appointments.findById(id).orElseThrow();
        checkCanTreat(a, auth);
        if (diagnosis.isBlank()) {
            model.addAttribute("appointment", a);
            model.addAttribute("diagnosis", diagnosis);
            model.addAttribute("treatment", treatment);
            model.addAttribute("notes", notes);
            model.addAttribute("error", "Diagnosis is required");
            return "visitForm";
        }
        a.setDiagnosis(diagnosis.trim());
        a.setTreatment(treatment);
        a.setNotes(notes);
        a.setStatus("COMPLETED");
        appointments.save(a);
        flash.addFlashAttribute("message", "Visit recorded and marked as completed.");
        return "redirect:/dashboard";
    }

    // ---------- PERMANENT DELETE - admin only ----------
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        LocalDate date = appointments.findById(id)
                .map(Appointment::getAppointmentDate).orElse(LocalDate.now());
        appointments.deleteById(id);
        flash.addFlashAttribute("message", "Booking permanently deleted.");
        return redirectToMonth(date);
    }

    // ---------- helpers ----------
    private void checkCanTreat(Appointment a, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        boolean assigned = a.getDoctor() != null && a.getDoctor().getUsername().equals(auth.getName());
        if (!admin && !assigned) {
            throw new AccessDeniedException("Only the assigned doctor can record this visit");
        }
    }

    private void attachPetAndDoctor(Appointment appt, Integer petId, Integer doctorId, BindingResult result) {
        Pet pet = (petId == null) ? null : pets.findById(petId).orElse(null);
        if (pet == null) {
            result.addError(new FieldError("appointment", "pet", "Please choose a pet"));
        } else {
            appt.setPet(pet);
        }
        AppUser doctor = (doctorId == null) ? null : users.findById(doctorId).orElse(null);
        if (doctor == null || !"DOCTOR".equals(doctor.getRole()) || !doctor.isEnabled()) {
            result.addError(new FieldError("appointment", "doctor", "Please choose a doctor"));
        } else {
            appt.setDoctor(doctor);
        }
    }

    /** A doctor cannot have two active bookings at the same date and time. */
    private void checkConflict(Appointment appt, Integer ignoreId, BindingResult result) {
        if (appt.getDoctor() == null || appt.getAppointmentDate() == null || appt.getAppointmentTime() == null) {
            return;
        }
        boolean clash = appointments
                .findByDoctorIdAndAppointmentDateAndAppointmentTime(
                        appt.getDoctor().getId(), appt.getAppointmentDate(), appt.getAppointmentTime())
                .stream()
                .anyMatch(o -> !o.getId().equals(ignoreId) && !"CANCELLED".equals(o.getStatus()));
        if (clash) {
            result.addError(new FieldError("appointment", "appointmentTime",
                    "This doctor already has a booking at that time"));
        }
    }

    private String redirectToMonth(LocalDate date) {
        return "redirect:/calendar?month=" + YearMonth.from(date);
    }
}
