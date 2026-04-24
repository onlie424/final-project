# BrainPath — Personalised Education Platform

A full-stack web application built as a final year project (CO3201) at the University of Leicester. BrainPath delivers personalised learning through an adaptive quiz system and recommendation engine, helping students identify and close their knowledge gaps.

---

## Project Structure

```
onnn1/
├── finalProjectB/          # Spring Boot backend (Java)
└── frontendfinalP-new/     # React frontend (Vite)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot, Spring Security, JWT |
| Frontend | React (Vite), React Router |
| Database | MySQL via JPA / Hibernate |
| Build Tools | Gradle (backend), npm (frontend) |

---

## Core Features

- **Student authentication** — register, log in, JWT-secured routes
- **Course enrolment** — students browse and enrol in available courses
- **Video-based lessons** — content delivered via embedded YouTube videos
- **Adaptive quiz system** — three difficulty tiers (Easy, Medium, Hard); students must pass each tier to progress. Questions are randomly sampled from a large question bank on every attempt so no two sittings are identical
- **Quiz resume** — if a student passes a tier but fails the next, their progress is saved and they can continue from where they left off
- **Personalised recommendations** — failed questions are mapped to the specific lesson that covers that topic; the system recommends exactly what to re-study
- **Student dashboard** — shows enrolled courses, current focus course, mastery gaps, and next steps
- **Admin dashboard** — create and delete courses, modules, lessons, and quizzes; assign each question to a lesson for accurate recommendations

---

## Running the Project

### Backend

Requirements: Java 17+, MySQL running locally

```bash
cd finalProjectB
./gradlew bootRun
```

The backend runs on `http://localhost:8080`.

### Frontend

Requirements: Node.js 18+

```bash
cd frontendfinalP-new
npm install
npm run dev
```

The frontend runs on `http://localhost:3000`.

---

## Repository Layout (Backend)

```
finalProjectB/src/main/java/com/example/finalprojectb/
├── controller/     # REST controllers (auth, course, module, lesson, quiz, dashboard)
├── service/        # Business logic (adaptive quiz, recommendations, enrolment)
├── model/          # JPA entities
├── repo/           # Spring Data repositories
├── DTO/            # Data Transfer Objects
├── security/       # JWT filter, user details service
└── Config/         # Spring Security and CORS configuration
```

---

## Running Tests

```bash
# Backend unit tests
cd finalProjectB
./gradlew test

# Frontend tests
cd frontendfinalP-new
npm test
```

---

## Author

Onlie Noel — University of Leicester, CO3201 Final Year Project, 2025/26
