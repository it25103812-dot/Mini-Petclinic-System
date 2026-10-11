package com.example.clinic;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates default accounts and sample data on first start (only when tables are empty). */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository users;
    private final OwnerRepository owners;
    private final PetRepository pets;
    private final AppointmentRepository appointments;
    private final PasswordEncoder encoder;

    public DataInitializer(AppUserRepository users, OwnerRepository owners, PetRepository pets,
                           AppointmentRepository appointments, PasswordEncoder encoder) {
        this.users = users;
        this.owners = owners;
        this.pets = pets;
        this.appointments = appointments;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            user("admin", "admin123", "Clinic Admin", null, "ADMIN");
            user("drsilva", "doctor123", "Dr. Silva", "General Practice", "DOCTOR");
            user("drperera", "doctor123", "Dr. Perera", "Surgery", "DOCTOR");
        }
        if (owners.count() == 0) {
            AppUser silva = users.findByUsername("drsilva").orElseThrow();
            AppUser perera = users.findByUsername("drperera").orElseThrow();
            LocalDate today = LocalDate.now();

            Owner george = owner("George", "Franklin", "Madison", "6085551023");
            Owner betty = owner("Betty", "Davis", "Sun Prairie", "6085551749");
            Owner eduardo = owner("Eduardo", "Rodriquez", "McFarland", "6085558763");
            Owner harold = owner("Harold", "Davis", "Windsor", "6085553198");

            Pet leo = pet(george, "Leo", "Cat", LocalDate.of(2020, 9, 7));
            Pet basil = pet(betty, "Basil", "Hamster", LocalDate.of(2022, 8, 6));
            Pet rosy = pet(eduardo, "Rosy", "Dog", LocalDate.of(2021, 4, 17));
            Pet jewel = pet(eduardo, "Jewel", "Dog", LocalDate.of(2023, 3, 7));
            Pet max = pet(harold, "Max", "Bird", LocalDate.of(2019, 11, 30));

            Appointment done = appt(leo, silva, today.minusDays(3), LocalTime.of(9, 30), "Vaccination");
            done.setStatus("COMPLETED");
            done.setDiagnosis("Healthy");
            done.setTreatment("Annual vaccine given");
            done.setNotes("Next vaccine due in 12 months");
            appointments.save(done);

            appt(rosy, silva, today, LocalTime.of(10, 0), "Regular check-up");
            appt(jewel, perera, today, LocalTime.of(14, 30), "Dental cleaning");
            appt(basil, silva, today.plusDays(1), LocalTime.of(11, 15), "Skin allergy");
            appt(max, perera, today.plusDays(3), LocalTime.of(16, 0), "Wing check");
            appt(leo, silva, today.plusDays(10), LocalTime.of(9, 0), "Follow-up visit");
        }
    }

    private void user(String username, String rawPassword, String fullName, String spec, String role) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        u.setFullName(fullName);
        u.setSpecialization(spec);
        u.setRole(role);
        u.setEnabled(true);
        users.save(u);
    }

    private Owner owner(String first, String last, String city, String phone) {
        Owner o = new Owner();
        o.setFirstName(first);
        o.setLastName(last);
        o.setCity(city);
        o.setTelephone(phone);
        return owners.save(o);
    }

    private Pet pet(Owner owner, String name, String type, LocalDate birth) {
        Pet p = new Pet();
        p.setName(name);
        p.setType(type);
        p.setBirthDate(birth);
        p.setOwner(owner);
        return pets.save(p);
    }

    private Appointment appt(Pet pet, AppUser doctor, LocalDate date, LocalTime time, String reason) {
        Appointment a = new Appointment();
        a.setPet(pet);
        a.setDoctor(doctor);
        a.setAppointmentDate(date);
        a.setAppointmentTime(time);
        a.setReason(reason);
        a.setStatus("PENDING");
        return appointments.save(a);
    }
}
