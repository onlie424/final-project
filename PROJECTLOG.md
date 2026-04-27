# Project Log

Please regularly update this file to record your project progress. You should be updating the project log _at least_ once a fortnight.

## Week 1 [w/c 22 Sep 2025]

- Project repository set up on GitLab by supervisor.
- Standard information and project documentation uploaded to the repository.
- Initial README created.

## Week 2 [w/c 03 Nov 2025]

- Completed and uploaded the User Stories document to the repository, defining the key requirements for both student and admin roles.
- Planned the core feature set: user authentication, course management, adaptive quiz system, and recommendation engine.

## Week 3 [w/c 24 Nov 2025]

- Set up the database schema and backend project structure.
- Configured the Spring Boot backend with initial project scaffolding and database connectivity.

## Week 4 [w/c 01 Dec 2025]

- Implemented login and registration endpoints on the backend.
- Created user authentication logic with JWT token generation.

## Week 5 [w/c 15 Dec 2025]

- Fixed bugs in login and registration backend logic.
- Completed the frontend login and registration pages; users are now redirected to the dashboard upon successful login.
- Resolved issues with the auth flow on the frontend.

## Week 6 [w/c 12 Jan 2026]

- Created Data Transfer Objects (DTOs) for the backend.
- Started implementation of `CourseService`, `ModuleService`, and `LessonService` with basic CRUD operations.

## Week 7 [w/c 19 Jan 2026]

- Added `.gitignore` for macOS `.DS_Store` files.
- Updated frontend dashboards and authentication pages with API integration connecting to the backend.
- Implemented the course controller, lesson controller, and module controller on the backend — create course, create lesson, and view requests are now functional.

## Week 8 [w/c 26 Jan 2026]

- Built the student dashboard frontend: course cards, navigation bar, and protected routes.
- Implemented the admin dashboard frontend.
- Added `VideoPlayer` component and course/admin service layers on the frontend.
- Added dashboard styles and fixed the auth flow with an `AuthProvider` wrapper.
- Merged the `User-and-Admin-Dashboard` branch into `main`.

## Week 9 [w/c 02 Feb 2026]

- Made further edits to the admin and user dashboards.
- Restored dashboard changes after a stash conflict.
- Implemented course creation and deletion functionality in the admin dashboard.
- Removed a duplicate frontend folder that was accidentally included inside the backend directory.

## Week 10 [w/c 16 Feb 2026]

- Merged the `6-course-enrollment` branch into `main`, bringing in course enrollment, admin dashboard updates, and role management.
- Removed the feature that allowed an admin to promote another user to admin (out of scope).
- Fixed code structure by removing all duplicate files from the repository.

## Week 11 [w/c 02 Mar 2026]

- Completely restructured the quiz implementation — redesigned the quiz data model and backend logic for a cleaner architecture.
- Started implementing updates to the quiz feature and began exploring an ML-based response/recommendation approach.

## Week 12 [w/c 09 Mar 2026]

- Created the quiz controller and module lock service on the backend.
- Made edits to the quiz service to support adding questions to quizzes.
- Implemented the admin-facing quiz management UI on the frontend so admins can create and manage quizzes and questions.
- Added the frontend quiz service layer.

## Week 13 [w/c 13 Mar 2026]

- Implemented the full quiz experience inside the classroom: students can now take quizzes, see which answers they got wrong, and view study recommendations.
- Rearranged the quiz flow to support resume functionality — if a user passes a difficulty level and fails the next, they can return and continue from where they left off.

## Week 14 [w/c 16 Mar 2026]

- Made CSS edits across the application to unify the colour scheme and overall styling consistency.
- Added a lesson dropdown directly on each question card in the admin quiz editor, allowing admins to assign each question to a specific lesson to improve the precision of the recommendation system.

## Week 15 [w/c 06 Apr 2026]

- Removed the machine learning feature and the PDF viewer from the classroom — content type is now video only, simplifying the architecture.

## Week 16 [w/c 13 Apr 2026]

- Removed the quiz time limit from the adaptive quiz.
- Fixed the admin navigation bar staying in place incorrectly after role changes.
- Deleted the initialiser script used for seeding admin accounts and quiz data (no longer needed).

## Week 17 [w/c 20 Apr 2026]

- Completed unit tests for the adaptive quiz service and the recommendation service — all test cases passing.
- Updated the dashboard Current Focus logic: when a student clicks into a course and enters the classroom, that course is now correctly highlighted as the current focus.
- Created unit test files for the quiz service and dashboard service (backend).
- Created frontend test files covering login, registration, and the recommendations system.

## Week 18 [w/c 27 Apr 2026]

- Added unit tests for `AuthService` and `EnrollmentService` to broaden backend test coverage.
- Configured JaCoCo in `build.gradle` to generate test coverage reports with a 70% branch coverage minimum.
- Cleaned up backend: removed unused commented-out reorder endpoints from `LessonController` and `ModuleController`, and removed the `loginStreak` field from the `User` model (feature was not implemented).
- Improved quiz review UX: replaced the question-mark icon on incorrect answer explanations with a clear "Explanation:" label for better student readability.
- Refactored frontend auth and dashboard services: cleaned up `authService`, `courseService`, `dashboardAPI`, `MyCourses`, and `Dashboard` components.
- Added a scope preamble to the User Stories document clarifying which features were deferred during iterative development, with references to the relevant dissertation sections.
