package com.example.clinic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    private final AppointmentRepository appointments;
    private final AppUserRepository users;

    public CalendarController(AppointmentRepository appointments, AppUserRepository users) {
        this.appointments = appointments;
        this.users = users;
    }

    /** One cell of the calendar grid. */
    public static class Day {
        private final LocalDate date;
        private final boolean inMonth;
        private final boolean today;
        private final boolean booked;
        private final List<Appointment> appointments;

        public Day(LocalDate date, boolean inMonth, boolean today, List<Appointment> appointments) {
            this.date = date;
            this.inMonth = inMonth;
            this.today = today;
            this.appointments = appointments;
            this.booked = appointments.stream().anyMatch(a -> !"CANCELLED".equals(a.getStatus()));
        }

        public LocalDate getDate() { return date; }
        public int getDayOfMonth() { return date.getDayOfMonth(); }
        public boolean isInMonth() { return inMonth; }
        public boolean isToday() { return today; }
        public boolean isBooked() { return booked; }
        public List<Appointment> getAppointments() { return appointments; }
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false)
                           @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                           @RequestParam(required = false) String scope,
                           Authentication auth, Model model) {
        YearMonth ym = (month != null) ? month : YearMonth.now();
        AppUser me = users.findByUsername(auth.getName()).orElseThrow();
        boolean admin = "ADMIN".equals(me.getRole());

        // Doctors see their own bookings by default; they can switch to "all".
        String sc = admin ? "all" : ("all".equals(scope) ? "all" : "mine");

        LocalDate start = ym.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = ym.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Appointment> all = "mine".equals(sc)
                ? appointments.findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
                        me.getId(), start, end)
                : appointments.findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(start, end);

        Map<LocalDate, List<Appointment>> byDate = all.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate));

        LocalDate today = LocalDate.now();
        List<List<Day>> weeks = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            List<Day> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                week.add(new Day(d, YearMonth.from(d).equals(ym), d.equals(today),
                        byDate.getOrDefault(d, List.of())));
                d = d.plusDays(1);
            }
            weeks.add(week);
        }

        List<Appointment> thisMonth = all.stream()
                .filter(a -> YearMonth.from(a.getAppointmentDate()).equals(ym))
                .toList();

        model.addAttribute("weeks", weeks);
        model.addAttribute("title", ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + ym.getYear());
        model.addAttribute("monthKey", ym.toString());
        model.addAttribute("prev", ym.minusMonths(1).toString());
        model.addAttribute("next", ym.plusMonths(1).toString());
        model.addAttribute("current", YearMonth.now().toString());
        model.addAttribute("scope", sc);
        model.addAttribute("appointments", thisMonth);
        return "calendar";
    }
}
