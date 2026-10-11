package com.example.clinic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
            LocalDate from, LocalDate to);

    List<Appointment> findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
            Integer doctorId, LocalDate from, LocalDate to);

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate date);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
            Integer doctorId, LocalDate date);

    List<Appointment> findByPetIdOrderByAppointmentDateDescAppointmentTimeDesc(Integer petId);

    List<Appointment> findByDoctorIdAndAppointmentDateAndAppointmentTime(
            Integer doctorId, LocalDate date, LocalTime time);
}
