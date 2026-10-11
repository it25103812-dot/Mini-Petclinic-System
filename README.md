# Mini PetClinic v4 (doctor side + login)

    mvn spring-boot:run

Open http://localhost:8080  -> login page

Default accounts (created on first start - CHANGE THESE PASSWORDS):
  admin     / admin123    (ADMIN)
  drsilva   / doctor123   (DOCTOR)
  drperera  / doctor123   (DOCTOR)

Roles
  DOCTOR: dashboard (today's bookings), owners, pets, bookings, record visits,
          cancel bookings. CANNOT permanently delete anything.
  ADMIN : everything a doctor can do + permanent delete (owners, pets, bookings)
          + manage doctor accounts + H2 console.
