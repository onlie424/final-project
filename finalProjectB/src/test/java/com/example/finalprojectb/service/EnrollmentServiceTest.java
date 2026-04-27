package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.CreateEnrollmentDTO;
import com.example.finalprojectb.DTO.EnrollmentDTO;
import com.example.finalprojectb.model.Course;
import com.example.finalprojectb.model.Enrollment;
import com.example.finalprojectb.model.User;
import com.example.finalprojectb.repo.CourseRepository;
import com.example.finalprojectb.repo.EnrollmentRepository;
import com.example.finalprojectb.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EnrollmentService.
 *
 * Includes a regression test for the lastAccessed timestamp bug discussed in
 * Sections 3.1.3 and 6.1.3 of the dissertation: touchLastAccessed must update
 * the timestamp on the existing enrollment, and must silently no-op rather than
 * throw when the enrollment does not exist.
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    // ---------- Shared fixtures ----------
    private User user;
    private Course publishedCourse;
    private Course unpublishedCourse;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setEmail("student@test.com");

        publishedCourse = new Course();
        publishedCourse.setId(10L);
        publishedCourse.setTitle("Course A");
        publishedCourse.setIsPublished(true);
        publishedCourse.setThumbnailUrl("a.png");

        unpublishedCourse = new Course();
        unpublishedCourse.setId(11L);
        unpublishedCourse.setTitle("Course B");
        unpublishedCourse.setIsPublished(false);

        enrollment = new Enrollment();
        enrollment.setId(99L);
        enrollment.setUser(user);
        enrollment.setCourse(publishedCourse);
        enrollment.setStatus("ACTIVE");
        enrollment.setCompletionPercentage(0.0);
        enrollment.setEnrollmentDate(LocalDateTime.now().minusDays(1));
        enrollment.setLastAccessed(LocalDateTime.now().minusDays(1));
    }

    private CreateEnrollmentDTO enrollmentRequest(long userId, long courseId) {
        CreateEnrollmentDTO dto = new CreateEnrollmentDTO();
        dto.setUserId(userId);
        dto.setCourseId(courseId);
        return dto;
    }

    // ============================================================
    //  enrollInCourse()
    // ============================================================

    @Nested
    class EnrollInCourse {

        @Test
        void enrollInCourse_validRequest_persistsActiveZeroPercentEnrollment() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(publishedCourse));
            when(enrollmentRepository.save(any(Enrollment.class)))
                    .thenAnswer(inv -> {
                        Enrollment e = inv.getArgument(0);
                        e.setId(42L);
                        return e;
                    });

            EnrollmentDTO result = enrollmentService.enrollInCourse(enrollmentRequest(1L, 10L));

            assertThat(result.getId()).isEqualTo(42L);
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getCompletionPercentage()).isEqualTo(0.0);
            assertThat(result.getCourseId()).isEqualTo(10L);
            assertThat(result.getCourseTitle()).isEqualTo("Course A");
            assertThat(result.getEnrollmentDate()).isNotNull();
            assertThat(result.getLastAccessed()).isNotNull();
        }

        @Test
        void enrollInCourse_alreadyEnrolled_throwsAndDoesNotPersist() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.enrollInCourse(enrollmentRequest(1L, 10L)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already enrolled");

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void enrollInCourse_unknownUser_throwsUserNotFound() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enrollInCourse(enrollmentRequest(1L, 10L)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        void enrollInCourse_unknownCourse_throwsCourseNotFound() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enrollInCourse(enrollmentRequest(1L, 10L)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Course not found");
        }

        @Test
        void enrollInCourse_unpublishedCourse_throwsAndDoesNotPersist() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 11L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(11L)).thenReturn(Optional.of(unpublishedCourse));

            assertThatThrownBy(() -> enrollmentService.enrollInCourse(enrollmentRequest(1L, 11L)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("unpublished");

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void enrollInCourse_setsBothEnrollmentDateAndLastAccessed() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(publishedCourse));
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

            enrollmentService.enrollInCourse(enrollmentRequest(1L, 10L));

            ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepository).save(captor.capture());
            Enrollment saved = captor.getValue();
            assertThat(saved.getEnrollmentDate()).isNotNull();
            assertThat(saved.getLastAccessed()).isNotNull();
            assertThat(saved.getCompletionPercentage()).isEqualTo(0.0);
            assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        }
    }

    // ============================================================
    //  touchLastAccessed()  --  regression test for Section 3.1.3 / 6.1.3
    // ============================================================

    @Nested
    class TouchLastAccessed {

        /**
         * Regression test for the bug described in Sections 3.1.3 and 6.1.3 of
         * the dissertation: lastAccessed was originally only set at enrolment
         * and never updated, breaking the dashboard's Current Focus selection.
         * touchLastAccessed must now refresh the timestamp on every classroom
         * load.
         */
        @Test
        void touchLastAccessed_existingEnrollment_updatesTimestampToNow() {
            LocalDateTime original = enrollment.getLastAccessed();
            when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(enrollment));

            LocalDateTime before = LocalDateTime.now();
            enrollmentService.touchLastAccessed(1L, 10L);
            LocalDateTime after = LocalDateTime.now();

            ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepository).save(captor.capture());
            LocalDateTime updated = captor.getValue().getLastAccessed();

            assertThat(updated)
                    .as("lastAccessed must advance, not remain at the original enrolment time")
                    .isAfter(original)
                    .isBetween(before, after);
        }

        @Test
        void touchLastAccessed_unknownEnrollment_silentlyNoOps() {
            when(enrollmentRepository.findByUserIdAndCourseId(99L, 99L)).thenReturn(Optional.empty());

            // Must not throw, must not save.
            enrollmentService.touchLastAccessed(99L, 99L);

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // ============================================================
    //  Read-side queries
    // ============================================================

    @Nested
    class ReadQueries {

        @Test
        void getUserEnrollments_validUser_returnsDtoList() {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(enrollmentRepository.findByUserId(1L)).thenReturn(List.of(enrollment));

            List<EnrollmentDTO> dtos = enrollmentService.getUserEnrollments(1L);

            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getCourseId()).isEqualTo(10L);
            assertThat(dtos.get(0).getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        void getUserEnrollments_unknownUser_throws() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> enrollmentService.getUserEnrollments(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            verify(enrollmentRepository, never()).findByUserId(any());
        }

        @Test
        void getCourseEnrollments_unknownCourse_throws() {
            when(courseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> enrollmentService.getCourseEnrollments(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Course not found");
        }

        @Test
        void getCourseEnrollments_validCourse_returnsDtoList() {
            when(courseRepository.existsById(10L)).thenReturn(true);
            when(enrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));

            List<EnrollmentDTO> dtos = enrollmentService.getCourseEnrollments(10L);

            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        void isEnrolled_delegatesToRepository() {
            when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

            assertThat(enrollmentService.isEnrolled(1L, 10L)).isTrue();
        }

        @Test
        void getEnrollmentById_existing_returnsDto() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));

            EnrollmentDTO dto = enrollmentService.getEnrollmentById(99L);

            assertThat(dto.getId()).isEqualTo(99L);
            assertThat(dto.getCourseTitle()).isEqualTo("Course A");
        }

        @Test
        void getEnrollmentById_missing_throws() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.getEnrollmentById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Enrollment not found");
        }

        @Test
        void getEnrollment_byUserAndCourse_existing_returnsDto() {
            when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(enrollment));

            EnrollmentDTO dto = enrollmentService.getEnrollment(1L, 10L);

            assertThat(dto.getCourseId()).isEqualTo(10L);
        }

        @Test
        void getEnrollment_byUserAndCourse_missing_throws() {
            when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.getEnrollment(1L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Enrollment not found");
        }
    }

    // ============================================================
    //  Mutations: unenroll / status / progress
    // ============================================================

    @Nested
    class Mutations {

        @Test
        void unenroll_existing_deletesEnrollment() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));

            enrollmentService.unenroll(99L);

            verify(enrollmentRepository).delete(enrollment);
        }

        @Test
        void unenroll_missing_throwsAndDoesNotDelete() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.unenroll(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Enrollment not found");

            verify(enrollmentRepository, never()).delete(any(Enrollment.class));
        }

        @Test
        void updateEnrollmentStatus_validStatus_updatesAndRefreshesLastAccessed() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
            LocalDateTime original = enrollment.getLastAccessed();

            EnrollmentDTO dto = enrollmentService.updateEnrollmentStatus(99L, "PAUSED");

            assertThat(dto.getStatus()).isEqualTo("PAUSED");
            assertThat(dto.getLastAccessed()).isAfter(original);
        }

        @Test
        void updateEnrollmentStatus_invalidStatus_throws() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));

            assertThatThrownBy(() -> enrollmentService.updateEnrollmentStatus(99L, "INVALID"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid status");

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void updateEnrollmentProgress_below100_keepsActiveStatus() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

            EnrollmentDTO dto = enrollmentService.updateEnrollmentProgress(99L, 65.0);

            assertThat(dto.getCompletionPercentage()).isEqualTo(65.0);
            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        void updateEnrollmentProgress_atOrAbove100_marksCompleted() {
            when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

            EnrollmentDTO dto = enrollmentService.updateEnrollmentProgress(99L, 100.0);

            assertThat(dto.getCompletionPercentage()).isEqualTo(100.0);
            assertThat(dto.getStatus()).isEqualTo("COMPLETED");
        }
    }
}
