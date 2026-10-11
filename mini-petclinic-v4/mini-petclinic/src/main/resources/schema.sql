CREATE TABLE IF NOT EXISTS app_users (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password       VARCHAR(100) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    user_role      VARCHAR(20)  NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS owners (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name  VARCHAR(50) NOT NULL,
    city       VARCHAR(80),
    telephone  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS pets (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    pet_type   VARCHAR(30) NOT NULL,
    owner_id   INT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    appt_date  DATE NOT NULL,
    appt_time  TIME NOT NULL,
    reason     VARCHAR(200) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    diagnosis  VARCHAR(1000),
    treatment  VARCHAR(1000),
    notes      VARCHAR(1000),
    pet_id     INT NOT NULL,
    doctor_id  INT NOT NULL,
    FOREIGN KEY (pet_id)    REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES app_users(id)
);
