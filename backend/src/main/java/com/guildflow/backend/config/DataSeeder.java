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

import java.time.LocalDate;
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
        private final StudentEvaluationRepository evaluationRepository;
        private final TaskProgressRepository progressRepository;
        private final PasswordEncoder passwordEncoder;
        private final RoomRepository roomRepository;
        private final RoomBookingRepository roomBookingRepository;

        @Override
        @SuppressWarnings("null")
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

                // --- Seed Goal Types earliest ---
                if (goalTypeRepository.findByName("Book Reading").isEmpty()) {
                        goalTypeRepository.save(GoalType.builder()
                                        .name("Book Reading")
                                        .description("Reading and tracking daily page counts.")
                                        .build());
                }
                if (goalTypeRepository.findByName("Weekly Homework").isEmpty()) {
                        goalTypeRepository.save(GoalType.builder()
                                        .name("Weekly Homework")
                                        .description("General tracking for weekly assignments.")
                                        .build());
                }

                // --- DUMMY DATA SEEDING ---

                // 3 Dummy Mentors
                String[] mentorNames = { "Zeynep", "Can", "Selin" };
                for (int i = 1; i <= 3; i++) {
                        String email = "mentor" + i + "@guildflow.com";
                        if (!userRepository.existsByEmail(email)) {
                                User mentor = User.builder()
                                                .email(email)
                                                .passwordHash(passwordEncoder.encode("mentor123"))
                                                .firstName(mentorNames[i - 1])
                                                .lastName("Hoca")
                                                .role(Role.MENTOR)
                                                .languagePref(LanguagePreference.TR)
                                                .build();
                                userRepository.save(mentor);
                                log.info("✅ Dummy mentor created: {}", email);
                        }
                }

                // 10 Dummy Students
                String[] studentFirstNames = { "Kerem", "Aslı", "Mert", "Ece", "Bora", "Işıl", "Arda", "Damla", "Kaan",
                                "Derya" };
                String[] studentLastNames = { "Yılmaz", "Demir", "Çelik", "Şahin", "Öztürk", "Kılıç", "Arslan", "Doğan",
                                "Yıldız", "Güneş" };
                for (int i = 1; i <= 10; i++) {
                        String email = "student" + i + "@guildflow.com";
                        if (!userRepository.existsByEmail(email)) {
                                User student = User.builder()
                                                .email(email)
                                                .passwordHash(passwordEncoder.encode("student123"))
                                                .firstName(studentFirstNames[i - 1])
                                                .lastName(studentLastNames[i - 1])
                                                .role(Role.STUDENT)
                                                .languagePref(LanguagePreference.TR)
                                                .build();
                                userRepository.save(student);
                                log.info("✅ Dummy student created: {}", email);
                        }
                }

                // 3 Dummy Classes
                String[] classNames = { "Science Explorers", "English Mastery", "History Buffs" };
                EducationLevel[] levels = { EducationLevel.HIGH_SCHOOL, EducationLevel.UNIVERSITY,
                                EducationLevel.SECONDARY };

                for (int i = 1; i <= 3; i++) {
                        final int index = i;
                        String mentorEmail = "mentor" + i + "@guildflow.com";
                        userRepository.findByEmail(mentorEmail).ifPresent(mentor -> {
                                if (mentorClassRepository.findByName(classNames[index - 1]).isEmpty()) {
                                        MentorClass dummyClass = MentorClass.builder()
                                                        .name(classNames[index - 1])
                                                        .educationLevel(levels[index - 1])
                                                        .description("Experimental dummy class for "
                                                                        + classNames[index - 1])
                                                        .mentor(mentor)
                                                        .build();
                                        mentorClassRepository.save(dummyClass);
                                        log.info("✅ Dummy class created: {}", classNames[index - 1]);
                                }
                        });
                }

                // Existing sample data (wrapped in a check or kept as is if compatible)
                // --- NEW: Create a sample class for the mentor ---
                userRepository.findByEmail("mentor@guildflow.com").ifPresent(mentor -> {
                        if (mentorClassRepository.findByActiveTrue().isEmpty()) {
                                MentorClass sampleClass = MentorClass.builder()
                                                .name("Saturday Math Group")
                                                .educationLevel(EducationLevel.SECONDARY)
                                                .description("Advanced math mentoring for secondary school students.")
                                                .mentor(mentor)
                                                .build();

                                MentorClass savedClass = mentorClassRepository.save(sampleClass);
                                log.info("✅ Sample class created: Saturday Math Group");

                                // Enroll student
                                userRepository.findByEmail("student@guildflow.com").ifPresent(student -> {
                                        ClassStudent enrollment = ClassStudent.builder()
                                                        .mentorClass(savedClass)
                                                        .student(student)
                                                        .build();
                                        classStudentRepository.save(enrollment);
                                        log.info("✅ Student Ali enrolled in Saturday Math Group");
                                });

                                // --- NEW: Create a sample Goal for the class ---
                                GoalType readingType = goalTypeRepository.findByName("Book Reading").orElse(null);
                                Goal mathGoal = Goal.builder()
                                                .title("Read 'Introduction to Algorithms'")
                                                .description("Read at least 10 pages per day.")
                                                .mentorClass(savedClass)
                                                .goalType(readingType)
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

                                // --- NEW: Seed some progress and evaluations ---
                                userRepository.findByEmail("student@guildflow.com").ifPresent(student -> {
                                        // Some reading progress
                                        progressRepository.save(TaskProgress.builder()
                                                        .student(student)
                                                        .task(readingTask)
                                                        .entryDate(java.time.LocalDate.now().minusDays(1))
                                                        .numericValue(4.0)
                                                        .build());
                                        progressRepository.save(TaskProgress.builder()
                                                        .student(student)
                                                        .task(readingTask)
                                                        .entryDate(java.time.LocalDate.now())
                                                        .numericValue(2.0)
                                                        .build());

                                        // A sample evaluation
                                        evaluationRepository.save(StudentEvaluation.builder()
                                                        .student(student)
                                                        .mentor(mentor)
                                                        .periodName("Initial Assessment")
                                                        .content("Ali shows great promise in logic. Needs to focus more on consistent reading habits.")
                                                        .build());
                                        log.info("✅ Sample progress and evaluation seeded for Ali");
                                });
                        }
                });

                // --- SEED GOAL TEMPLATES ---
                if (goalRepository.findByTitle("English Vocabulary Master").isEmpty()) {
                        userRepository.findByEmail("admin@guildflow.com").ifPresent(admin -> {
                                GoalType homework = goalTypeRepository.findByName("Weekly Homework").orElse(null);
                                GoalType reading = goalTypeRepository.findByName("Book Reading").orElse(null);

                                if (homework == null || reading == null) {
                                        log.warn("⚠️ Skipping template seeding because GoalTypes were not found.");
                                        return;
                                }

                                // 1. Vocabulary Master
                                Goal v1 = goalRepository.save(Goal.builder()
                                                .title("English Vocabulary Master")
                                                .description("Learn 50 new words this week and use them in sentences.")
                                                .isTemplate(true)
                                                .goalType(homework)
                                                .createdBy(admin)
                                                .active(true)
                                                .build());
                                goalTaskRepository.save(GoalTask.builder().goal(v1).title("New Words Learned").taskType(TaskType.NUMBER).targetValue(50.0).build());

                                // 2. Reading Marathon
                                Goal v2 = goalRepository.save(Goal.builder()
                                                .title("Classic Literature Marathon")
                                                .description("Read a classic novel and summarize each chapter.")
                                                .isTemplate(true)
                                                .goalType(reading)
                                                .createdBy(admin)
                                                .active(true)
                                                .build());
                                goalTaskRepository.save(GoalTask.builder().goal(v2).title("Pages Read").taskType(TaskType.NUMBER).targetValue(200.0).build());
                                goalTaskRepository.save(GoalTask.builder().goal(v2).title("Chapters Summarized").taskType(TaskType.NUMBER).targetValue(10.0).build());

                                // 3. Math Wizard
                                Goal v3 = goalRepository.save(Goal.builder()
                                                .title("Calculus Practice Set")
                                                .description("Complete advanced calculus problems from the workbook.")
                                                .isTemplate(true)
                                                .goalType(homework)
                                                .createdBy(admin)
                                                .active(true)
                                                .build());
                                goalTaskRepository.save(GoalTask.builder().goal(v3).title("Solved Problems").taskType(TaskType.NUMBER).targetValue(30.0).build());

                                // 4. Science Experiment
                                Goal v4 = goalRepository.save(Goal.builder()
                                                .title("Weekly Lab Diary")
                                                .description("Record observations from science lab experiments.")
                                                .isTemplate(true)
                                                .goalType(homework)
                                                .createdBy(admin)
                                                .active(true)
                                                .build());
                                goalTaskRepository.save(GoalTask.builder().goal(v4).title("Logs Entered").taskType(TaskType.NUMBER).targetValue(5.0).build());

                                // 5. Daily Reflection
                                Goal v5 = goalRepository.save(Goal.builder()
                                                .title("Self-Growth Journal")
                                                .description("Write a short reflection about today's learning journey.")
                                                .isTemplate(true)
                                                .goalType(homework)
                                                .createdBy(admin)
                                                .active(true)
                                                .build());
                                goalTaskRepository.save(GoalTask.builder().goal(v5).title("Daily Entries").taskType(TaskType.NUMBER).targetValue(7.0).build());

                                log.info("✅ 5 dummy goal templates seeded");
                        });
                }

                // --- SEED ROOMS ---
                if (!roomRepository.existsByTitle("Conference Room A")) {
                        userRepository.findByEmail("admin@guildflow.com").ifPresent(admin -> {
                                userRepository.findByEmail("mentor@guildflow.com").ifPresent(mentor -> {
                                        userRepository.findByEmail("mentor1@guildflow.com").ifPresent(mentor1 -> {

                                                // Create 5 rooms
                                                Room roomA = roomRepository.save(Room.builder()
                                                        .title("Conference Room A")
                                                        .description("Large meeting room with projector and whiteboard. Capacity: 20 seats.")
                                                        .capacity(20)
                                                        .canStayOvernight(false)
                                                        .build());

                                                Room roomB = roomRepository.save(Room.builder()
                                                        .title("Study Room B")
                                                        .description("Quiet individual or small group study room. No noise policy applies.")
                                                        .capacity(6)
                                                        .canStayOvernight(false)
                                                        .build());

                                                Room roomC = roomRepository.save(Room.builder()
                                                        .title("Computer Lab")
                                                        .description("30 workstations with high-speed internet. Ideal for digital exercises.")
                                                        .capacity(30)
                                                        .canStayOvernight(false)
                                                        .build());

                                                Room roomD = roomRepository.save(Room.builder()
                                                        .title("Seminar Hall")
                                                        .description("Large hall for seminars, presentations and group activities.")
                                                        .capacity(50)
                                                        .canStayOvernight(false)
                                                        .build());

                                                Room roomE = roomRepository.save(Room.builder()
                                                        .title("Overnight Study Hall")
                                                        .description("24-hour accessible study hall for intensive study sessions. Meals allowed.")
                                                        .capacity(15)
                                                        .canStayOvernight(true)
                                                        .build());

                                                log.info("✅ 5 rooms seeded");

                                                // Seed bookings for today
                                                LocalDateTime today = LocalDate.now().atStartOfDay();

                                                // Room A — 09:00-11:00 admin / 13:00-14:00 mentor
                                                roomBookingRepository.save(RoomBooking.builder().room(roomA).bookedBy(admin).reason("Weekly Team Sync").startTime(today.plusHours(9)).endTime(today.plusHours(11)).build());
                                                roomBookingRepository.save(RoomBooking.builder().room(roomA).bookedBy(mentor).reason("Student Progress Review").startTime(today.plusHours(13)).endTime(today.plusHours(14)).build());

                                                // Room B — 10:00-12:00 mentor1
                                                roomBookingRepository.save(RoomBooking.builder().room(roomB).bookedBy(mentor1).reason("One-on-One Tutoring").startTime(today.plusHours(10)).endTime(today.plusHours(12)).build());

                                                // Room C — 09:00-13:00 admin (all morning)
                                                roomBookingRepository.save(RoomBooking.builder().room(roomC).bookedBy(admin).reason("Digital Skills Workshop").startTime(today.plusHours(9)).endTime(today.plusHours(13)).build());

                                                // Room D — 14:00-17:00 mentor (afternoon seminar)
                                                roomBookingRepository.save(RoomBooking.builder().room(roomD).bookedBy(mentor).reason("Spring Enrollment Seminar").startTime(today.plusHours(14)).endTime(today.plusHours(17)).build());

                                                // Room E — 20:00-23:00 mentor1 (evening overnight)
                                                roomBookingRepository.save(RoomBooking.builder().room(roomE).bookedBy(mentor1).reason("Exam Preparation Night Session").startTime(today.plusHours(20)).endTime(today.plusHours(23)).build());

                                                log.info("✅ 6 dummy room bookings seeded for today");
                                        });
                                });
                        });
                }
        }
}
