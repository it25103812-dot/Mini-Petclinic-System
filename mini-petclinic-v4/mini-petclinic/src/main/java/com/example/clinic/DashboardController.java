package com.example.clinic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final AppointmentRepository appointments;
    private final AppUserRepository users;
    private final OwnerRepository owners;
    private final PetRepository pets;

    public DashboardController(AppointmentRepository appointments, AppUserRepository users,
                               OwnerRepository owners, PetRepository pets) {
        this.appointments = appointments;
        this.users = users;
        this.owners = owners;
        this.pets = pets;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        AppUser me = users.findByUsername(auth.getName()).orElseThrow();
        boolean admin = "ADMIN".equals(me.getRole());
        LocalDate today = LocalDate.now();

        List<Appointment> todays = admin
                ? appointments.findByAppointmentDateOrderByAppointmentTimeAsc(today)
                : appointments.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(me.getId(), today);
        List<Appointment> upcoming = admin
                ? appointments.findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
                        today.plusDays(1), today.plusDays(7))
                : appointments.findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
                        me.getId(), today.plusDays(1), today.plusDays(7));

        long pendingToday = todays.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long doneToday = todays.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();

        model.addAttribute("todayText", today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH)));
        model.addAttribute("todays", todays);
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("pendingToday", pendingToday);
        model.addAttribute("doneToday", doneToday);
        model.addAttribute("ownerCount", owners.count());
        model.addAttribute("petCount", pets.count());
        return "dashboard";
    }
}
