package com.guildflow.backend.config;

import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.EducationLevel;
import com.guildflow.backend.model.enums.LanguagePreference;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.model.enums.TaskType;
import com.guildflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MentorClassRepository mentorClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final GoalTypeRepository goalTypeRepository;
    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final MeetingRepository meetingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByEmail("admin@guildflow.com")) {
            User admin = User.builder()
                    .email("admin@guildflow.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Admin")
                    .role(Role.ADMIN)
                    .languagePref(LanguagePreference.EN)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin user created: admin@guildflow.com / admin123");
        }

        // Create a sample mentor for testing
        if (!userRepository.existsByEmail("mentor@guildflow.com")) {
            User mentor = User.builder()
                    .email("mentor@guildflow.com")
                    .passwordHash(passwordEncoder.encode("mentor123"))
                    .firstName("Ahmet")
                    .lastName("Yılmaz")
                    .role(Role.MENTOR)
                    .languagePref(LanguagePreference.TR)
                    .build();
            userRepository.save(mentor);
            log.info("✅ Sample mentor created: mentor@guildflow.com / mentor123");
        }

        // Create a sample student for testing
        if (!userRepository.existsByEmail("student@guildflow.com")) {
            User student = User.builder()
                    .email("student@guildflow.com")
                    .passwordHash(passwordEncoder.encode("student123"))
                    .firstName("Ali")
                    .lastName("Kaya")
                    .role(Role.STUDENT)
                    .languagePref(LanguagePreference.TR)
                    .build();
            userRepository.save(student);
            log.info("✅ Sample student created: student@guildflow.com / student123");
        }

        // Create a sample parent for testing
        if (!userRepository.existsByEmail("parent@guildflow.com")) {
            User parent = User.builder()
                    .email("parent@guildflow.com")
                    .passwordHash(passwordEncoder.encode("parent123"))
                    .firstName("Mehmet")
                    .lastName("Kaya")
                    .role(Role.PARENT)
                    .languagePref(LanguagePreference.TR)
                    .build();
            userRepository.save(parent);
            log.info("✅ Sample parent created: parent@guildflow.com / parent123");
        }

        // --- NEW: Create a sample class for the mentor ---
        User mentor = userRepository.findByEmail("mentor@guildflow.com").orElse(null);
        if (mentor != null && mentorClassRepository.findByActiveTrue().isEmpty()) {
            MentorClass sampleClass = MentorClass.builder()
                    .name("Saturday Math Group")
                    .educationLevel(EducationLevel.SECONDARY)
                    .description("Advanced math mentoring for secondary school students.")
                    .mentor(mentor)
                    .build();

            MentorClass savedClass = mentorClassRepository.save(sampleClass);
            log.info("✅ Sample class created: Saturday Math Group");

            // Enroll student
            User student = userRepository.findByEmail("student@guildflow.com").orElse(null);
            if (student != null) {
                ClassStudent enrollment = ClassStudent.builder()
                        .mentorClass(savedClass)
                        .student(student)
                        .build();
                classStudentRepository.save(enrollment);
                log.info("✅ Student Ali enrolled in Saturday Math Group");
            }

            // --- NEW: Seed Goal Types ---
            GoalType bookReading = goalTypeRepository.save(GoalType.builder()
                    .name("Book Reading")
                    .description("Reading and tracking daily page counts.")
                    .build());

            goalTypeRepository.save(GoalType.builder()
                    .name("Weekly Homework")
                    .description("General tracking for weekly assignments.")
                    .build());

            log.info("✅ Goal types seeded");

            // --- NEW: Create a sample Goal for the class ---
            Goal mathGoal = Goal.builder()
                    .title("Read 'Introduction to Algorithms'")
                    .description("Read at least 10 pages per day.")
                    .mentorClass(savedClass)
                    .goalType(bookReading)
                    .applyToAll(true)
                    .build();

            Goal savedGoal = goalRepository.save(mathGoal);

            // Add a task to this goal
            GoalTask readingTask = GoalTask.builder()
                    .goal(savedGoal)
                    .title("Pages read today")
                    .taskType(TaskType.NUMBER)
                    .targetValue(10.0)
                    .build();

            goalTaskRepository.save(readingTask);
            log.info("✅ Sample goal with tasks created for Saturday Math Group");

            // --- NEW: Create a sample Meeting ---
            Meeting mathMeeting = Meeting.builder()
                    .title("Introduction to Algebra Session")
                    .description("First theoretical lesson on algebra basics.")
                    .startTime(LocalDateTime.now().plusHours(2))
                    .endTime(LocalDateTime.now().plusHours(4))
                    .location("Room 101 / Zoom")
                    .mentorClass(savedClass)
                    .build();

            meetingRepository.save(mathMeeting);
            log.info("✅ Sample meeting created for Saturday Math Group");
        }
    }
}
