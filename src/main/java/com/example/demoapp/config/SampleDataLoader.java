package com.example.demoapp.config;

import com.example.demoapp.entity.*;
import com.example.demoapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Inserts realistic-looking users (customers + Mahirs), jobs, bids, bookings, chat threads/messages,
 * and a completed review so the app does not feel empty on first run.
 * <p>
 * Enable with {@code app.sample-data.enabled=true}, or {@code APP_SAMPLE_DATA=true}.
 * Legacy alias: {@code APP_DEMO_SEED=true} (see {@code application.properties}).
 */
@Component
@Order(200)
@ConditionalOnProperty(name = "app.sample-data.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SampleDataLoader implements ApplicationRunner {

    /**
     * If this user already exists, the loader assumes sample data was applied (idempotent).
     */
    public static final String SEED_MARKER_EMAIL = "nadia.mansour@gmail.com";

    /** Shared initial password for all seeded accounts (change after first login in production). */
    public static final String SEED_PLAINTEXT_PASSWORD = "Password123!";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JobRepository jobRepository;
    private final BidRepository bidRepository;
    private final BookingRepository bookingRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            String enc = passwordEncoder.encode(SEED_PLAINTEXT_PASSWORD);
            if (userRepository.existsByEmail(SEED_MARKER_EMAIL)) {
                return;
            }
            if (categoryRepository.count() == 0) {
                log.warn("Sample data skipped: no categories (CategoryDataLoader may have failed).");
                return;
            }

            Category plumbing = cat("Plumbing");
            Category electrical = cat("Electrician");
            Category cleaning = cat("Home Cleaning");
            Category carpentry = cat("Carpentry");
            Category painting = cat("Painting & Renovation");
            Category ac = cat("AC Services");
            Category pest = cat("Pest Control");
            Category moving = cat("Home Shifting");
            Category landscaping = cat("Handyman Services");

            User nadia = userRepository.save(User.builder()
                    .fullName("Nadia Mansour")
                    .email(SEED_MARKER_EMAIL)
                    .password(enc)
                    .phoneNumber("+216 55 112 203")
                    .dateOfBirth(LocalDate.of(1991, 7, 22))
                    .location(loc("Avenue Habib Bourguiba, Tunis", 36.8065, 10.1815))
                    .accountType(AccountType.PREMIUM)
                    .role(Role.USER)
                    .build());

            User mohamed = userRepository.save(User.builder()
                    .fullName("Mohamed Trabelsi")
                    .email("mohamed.trabelsi@outlook.com")
                    .password(enc)
                    .phoneNumber("+216 55 334 881")
                    .location(loc("Lac 2, Tunis", 36.8890, 10.2460))
                    .accountType(AccountType.FREEMIUM)
                    .role(Role.USER)
                    .build());

            User leila = userRepository.save(User.builder()
                    .fullName("Leila Hamdi")
                    .email("leila.hamdi@yahoo.com")
                    .password(enc)
                    .phoneNumber("+216 55 778 014")
                    .location(loc("Sousse centre", 35.8256, 10.6411))
                    .accountType(AccountType.FREEMIUM)
                    .role(Role.USER)
                    .build());

            User ahmed = mahir("Ahmed Hassen", "ahmed.hassen.plumbing@gmail.com", enc,
                    "Certified plumber, 10+ years. Emergency leaks and bathroom installs.",
                    List.of(plumbing, electrical));
            User marco = mahir("Marco Rossi", "marco.rossi.craft@icloud.com", enc,
                    "Carpenter and small repairs. Custom shelves and doors.",
                    List.of(carpentry, painting));
            User sofia = mahir("Sofia Ben Youssef", "sofia.benyoussef.pro@gmail.com", enc,
                    "Deep cleaning and move-out packages. Eco products on request.",
                    List.of(cleaning));
            User karim = mahir("Karim Mezzi", "karim.mezzi.hvac@gmail.com", enc,
                    "AC install, maintenance, and split units.",
                    List.of(ac));
            User priya = mahir("Priya Nair", "priya.nair.electric@gmail.com", enc,
                    "Licensed electrician. Panels, lighting, and safety checks.",
                    List.of(electrical, pest));
            User yanis = mahir("Yanis Bouazizi", "yanis.bouazizi.land@gmail.com", enc,
                    "Garden design, irrigation, and heavy lifting for moves.",
                    List.of(landscaping, moving));
            User sami = mahir("Sami Dridi", "sami.dridi.services@gmail.com", enc,
                    "Plumbing assist and post-construction cleaning.",
                    List.of(plumbing, cleaning));

            Instant now = Instant.now();

            Job j1 = job(nadia, plumbing, now, "Urgent leak under kitchen sink",
                    "Water pooling under the cabinet since this morning. Need someone today if possible.",
                    "Les Berges du Lac, Tunis", 36.8892, 10.2458,
                    new BigDecimal("80"), new BigDecimal("150"), 2, JobStatus.OPEN);
            Job j2 = job(mohamed, electrical, now, "Install new ceiling lights in living room",
                    "Four pendant fixtures; wiring exists from old fittings.",
                    "El Menzah, Tunis", 36.8311, 10.1711,
                    new BigDecimal("120"), new BigDecimal("220"), 4, JobStatus.OPEN);
            Job j3 = job(leila, cleaning, now, "Post-renovation apartment clean",
                    "85 m², dust and paint splashes. Windows included.",
                    "Sousse", 35.8256, 10.6411,
                    new BigDecimal("90"), new BigDecimal("160"), 6, JobStatus.OPEN);
            Job j4 = job(nadia, painting, now, "Two bedrooms + hallway repaint",
                    "Walls only, neutral white. We supply paint.",
                    "La Marsa, Tunis", 36.8784, 10.3247,
                    new BigDecimal("200"), new BigDecimal("400"), 12, JobStatus.OPEN);
            Job j5 = job(mohamed, carpentry, now, "Built-in wardrobe measurements + build",
                    "Alcove 2.4m wide, floor to ceiling preferred.",
                    "Ariana, Tunis", 36.8600, 10.1930,
                    new BigDecimal("350"), new BigDecimal("700"), 16, JobStatus.OPEN);
            Job j6 = job(leila, ac, now, "Split AC service + gas refill",
                    "Unit blows warm air. Brand Daikin, about three years old.",
                    "Hammamet", 36.4000, 10.6167,
                    new BigDecimal("60"), new BigDecimal("120"), 2, JobStatus.OPEN);

            Job j7 = job(nadia, plumbing, now.minus(2, ChronoUnit.DAYS), "Low water pressure in shower",
                    "Pressure dropped over the last week. Other taps are fine.",
                    "Carthage", 36.8611, 10.3311,
                    new BigDecimal("55"), new BigDecimal("120"), 3, JobStatus.OPEN);
            Job j8 = job(mohamed, electrical, now.minus(1, ChronoUnit.DAYS), "Extra wall sockets in home office",
                    "Need four double sockets, trunking acceptable.",
                    "Ben Arous", 36.7531, 10.2189,
                    new BigDecimal("140"), new BigDecimal("260"), 5, JobStatus.OPEN);
            Job j9 = job(leila, cleaning, now.minus(5, ChronoUnit.DAYS), "Monthly studio cleaning",
                    "Small studio near the beach; usual monthly visit.",
                    "Gammarth", 36.9167, 10.2833,
                    new BigDecimal("45"), new BigDecimal("85"), 3, JobStatus.OPEN);

            List<Job> jobs = jobRepository.saveAll(List.of(j1, j2, j3, j4, j5, j6, j7, j8, j9));

            Job openLeak = jobs.get(0);
            Job openLights = jobs.get(1);
            Job openClean = jobs.get(2);
            Job openPaint = jobs.get(3);
            Job openWardrobe = jobs.get(4);
            Job openAc = jobs.get(5);
            Job jobShower = jobs.get(6);
            Job jobSockets = jobs.get(7);
            Job jobStudio = jobs.get(8);

            bid(openLeak, ahmed, new BigDecimal("95"), "I can come this afternoon.", 2);
            bid(openLeak, sami, new BigDecimal("88"), "Available after 4pm today.", 2);
            bid(openLights, priya, new BigDecimal("180"), "Full install and testing included.", 4);
            bid(openClean, sofia, new BigDecimal("120"), "Team of two, half day.", 5);
            bid(openPaint, marco, new BigDecimal("320"), "Two coats, prep included.", 10);
            bid(openWardrobe, marco, new BigDecimal("520"), "MDF internals, soft-close hinges.", 14);
            bid(openAc, karim, new BigDecimal("85"), "Diagnostic and refill if needed.", 2);
            bid(openAc, priya, new BigDecimal("75"), "Can check electrics to the outdoor unit if you want.", 3);

            Bid showerBidAhmed = bidSaved(jobShower, ahmed, new BigDecimal("90"),
                    "Likely limescale in the mixer; I can descale or replace cartridges.", 3);
            Bid showerBidSami = bidSaved(jobShower, sami, new BigDecimal("85"),
                    "Happy to inspect tomorrow morning.", 3);
            Booking bShower = acceptJobWithBooking(jobShower, showerBidAhmed, showerBidSami, BookingStatus.ACCEPTED);

            Bid socketBidPriya = bidSaved(jobSockets, priya, new BigDecimal("195"),
                    "Materials included; half day.", 5);
            Bid socketBidAhmed = bidSaved(jobSockets, ahmed, new BigDecimal("160"),
                    "I can run the cable extensions; minor wall patching.", 6);
            Booking bSockets = acceptJobWithBooking(jobSockets, socketBidPriya, socketBidAhmed,
                    BookingStatus.IN_PROGRESS);

            Bid studioBid = bidSaved(jobStudio, sofia, new BigDecimal("65"),
                    "Same slot as usual, eco products.", 3);
            Booking bStudio = acceptJobWithBooking(jobStudio, studioBid, null, BookingStatus.COMPLETED);

            ChatThread tShower = chatThreadRepository.findByBookingId(bShower.getId()).orElseThrow();
            Instant t0 = now.minus(2, ChronoUnit.DAYS);
            message(tShower, nadia, "Hi Ahmed, thanks for accepting. Can you come Saturday morning?", t0);
            message(tShower, ahmed, "Hi Nadia — yes, 9am works for me.", t0.plus(12, ChronoUnit.MINUTES));
            message(tShower, nadia, "Perfect. Parking is in the basement, slot B7.", t0.plus(20, ChronoUnit.MINUTES));
            message(tShower, ahmed, "Noted. I will bring spare cartridges in case.", t0.plus(25, ChronoUnit.MINUTES));
            message(tShower, nadia, "Great, see you then.", t0.plus(30, ChronoUnit.MINUTES));

            // Chat: electrical (Mohamed ↔ Priya) — in progress
            ChatThread tSockets = chatThreadRepository.findByBookingId(bSockets.getId()).orElseThrow();
            Instant u0 = now.minus(6, ChronoUnit.HOURS);
            message(tSockets, mohamed, "Hi Priya, the office is unlocked. Wi‑Fi password is on the fridge.", u0);
            message(tSockets, priya, "Thanks — starting now. I will send photos of trunking before I close the walls.", u0.plus(8, ChronoUnit.MINUTES));
            message(tSockets, priya, "First two sockets live-tested OK.", u0.plus(95, ChronoUnit.MINUTES));
            message(tSockets, mohamed, "Looks neat, thanks for the update.", u0.plus(100, ChronoUnit.MINUTES));

            ChatThread tStudio = chatThreadRepository.findByBookingId(bStudio.getId()).orElseThrow();
            Instant v0 = now.minus(4, ChronoUnit.DAYS);
            message(tStudio, leila, "Hi Sofia, same time next week works?", v0);
            message(tStudio, sofia, "Yes — Thursday 10am confirmed.", v0.plus(10, ChronoUnit.MINUTES));
            message(tStudio, leila, "Left the key with the guard as usual.", v0.plus(1, ChronoUnit.DAYS));
            message(tStudio, sofia, "All done, floors dried. Have a good week!", v0.plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS));

            reviewRepository.save(Review.builder()
                    .booking(bStudio)
                    .reviewer(leila)
                    .mahir(sofia)
                    .rating(5)
                    .comment("Always punctual and thorough. Studio smells fresh every time.")
                    .build());

            log.info("Sample data loaded: customers, Mahirs, jobs, bookings, chat, review. "
                            + "Customer login: {} / {} | Admin: {} / {}",
                    SEED_MARKER_EMAIL, SEED_PLAINTEXT_PASSWORD,
                    PlatformAdminSeedLoader.SEED_ADMIN_EMAIL, SEED_PLAINTEXT_PASSWORD);
        } catch (Exception e) {
            log.warn("Sample data load failed (safe to ignore if DB already customized): {}", e.getMessage());
        }
    }

    private Category cat(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Missing category: " + name));
    }

    private Location loc(String street, double lat, double lon) {
        return Location.builder().streetAddress(street).latitude(lat).longitude(lon).build();
    }

    private User mahir(String fullName, String email, String encPassword, String bio, List<Category> categories) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .password(encPassword)
                .phoneNumber("+216 55 9" + String.format("%02d", Math.abs(email.hashCode() % 100)) + " "
                        + String.format("%03d", Math.abs(email.hashCode() % 1000)))
                .location(loc("Greater Tunis area", 36.85, 10.20))
                .accountType(AccountType.FREEMIUM)
                .role(Role.MAHIR)
                .serviceCategories(categories)
                .bio(bio)
                .credits(5)
                .build());
    }

    private Job job(User postedBy, Category category, Instant createdAt, String title, String description,
                    String street, double lat, double lon,
                    BigDecimal budgetMin, BigDecimal budgetMax, int durationHours, JobStatus status) {
        return Job.builder()
                .postedBy(postedBy)
                .category(category)
                .title(title)
                .description(description)
                .location(loc(street, lat, lon))
                .scheduledAt(LocalDateTime.now().plusDays(3 + (title.length() % 5)))
                .budgetMin(budgetMin)
                .budgetMax(budgetMax)
                .durationHours(durationHours)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private void bid(Job job, User mahir, BigDecimal price, String message, int estHours) {
        bidRepository.save(Bid.builder()
                .job(job)
                .mahir(mahir)
                .message(message)
                .proposedPrice(price)
                .proposedAt(LocalDateTime.now())
                .estimatedDurationHours(estHours)
                .status(BidStatus.PENDING)
                .build());
    }

    private Bid bidSaved(Job job, User mahir, BigDecimal price, String message, int estHours) {
        return bidRepository.save(Bid.builder()
                .job(job)
                .mahir(mahir)
                .message(message)
                .proposedPrice(price)
                .proposedAt(LocalDateTime.now())
                .estimatedDurationHours(estHours)
                .status(BidStatus.PENDING)
                .build());
    }

    /**
     * Marks winning bid accepted, optional other rejected, job assigned or completed, persists booking + chat thread.
     */
    private Booking acceptJobWithBooking(Job job, Bid accepted, Bid rejectedOther, BookingStatus bookingStatus) {
        accepted.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(accepted);
        if (rejectedOther != null && rejectedOther.getStatus() == BidStatus.PENDING) {
            rejectedOther.setStatus(BidStatus.REJECTED);
            bidRepository.save(rejectedOther);
        }
        if (bookingStatus == BookingStatus.COMPLETED) {
            job.setStatus(JobStatus.COMPLETED);
        } else {
            job.setStatus(JobStatus.ASSIGNED);
        }
        jobRepository.save(job);

        Booking booking = Booking.builder()
                .customer(job.getPostedBy())
                .mahir(accepted.getMahir())
                .job(job)
                .bid(accepted)
                .agreedPrice(accepted.getProposedPrice())
                .status(bookingStatus)
                .scheduledAt(accepted.getProposedAt() != null ? accepted.getProposedAt() : job.getScheduledAt())
                .message(accepted.getMessage())
                .build();
        booking = bookingRepository.save(booking);
        chatThreadRepository.save(ChatThread.builder().booking(booking).build());
        return booking;
    }

    private void message(ChatThread thread, User sender, String content, Instant createdAt) {
        chatMessageRepository.save(ChatMessage.builder()
                .thread(thread)
                .sender(sender)
                .content(content)
                .createdAt(createdAt)
                .build());
    }
}
