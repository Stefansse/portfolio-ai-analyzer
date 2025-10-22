package com.example.analyticsservice.web;

import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.model.dto.AnalyticsDTO;
import com.example.analyticsservice.model.dto.SkillProgressOverviewDTO;
import com.example.analyticsservice.repo.AnalyticsRepository;
import com.example.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsService analyticsService;

    // 🧩 1. Save raw analytics (for testing or direct save)
    @PostMapping
    public ResponseEntity<Void> saveAnalytics(@RequestBody Analytics analytics) {
        analyticsRepository.save(analytics);
        return ResponseEntity.ok().build();
    }

    // 🧠 2. Save analytics using DTO (from ResumeService)
    @PostMapping("/dto")
    public ResponseEntity<AnalyticsDTO> saveAnalyticsDTO(@RequestBody AnalyticsDTO dto) {
        AnalyticsDTO saved = analyticsService.saveAnalytics(dto);
        return ResponseEntity.ok(saved);
    }

    // 📦 3. Get all analytics for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnalyticsDTO>> getUserAnalytics(@PathVariable Long userId) {
        List<AnalyticsDTO> analyticsList = analyticsService.getAnalyticsByUser(userId);
        return ResponseEntity.ok(analyticsList);
    }

    // 📈 4. Get skill progress overview (trend chart)
    @GetMapping("/progress/{userId}")
    public ResponseEntity<SkillProgressOverviewDTO> getSkillProgress(@PathVariable Long userId) {
        SkillProgressOverviewDTO overview = analyticsService.getSkillProgressForUser(userId);
        return ResponseEntity.ok(overview);
    }

    // 🧮 5. Get the latest analytics record for a user
    @GetMapping("/latest/{userId}")
    public ResponseEntity<AnalyticsDTO> getLatestAnalytics(@PathVariable Long userId) {
        AnalyticsDTO latest = analyticsService.getLatestAnalyticsForUser(userId);
        if (latest == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(latest);
    }

    // 🕓 6. Get total resume upload count for a user
    @GetMapping("/count/{userId}")
    public ResponseEntity<Long> getUserUploadCount(@PathVariable Long userId) {
        long count = analyticsService.getUserUploadCount(userId);
        return ResponseEntity.ok(count);
    }

    // 🔄 7. Compare the last two uploads
    @GetMapping("/compare/{userId}")
    public ResponseEntity<SkillProgressOverviewDTO> compareLastTwoUploads(@PathVariable Long userId) {
        SkillProgressOverviewDTO comparison = analyticsService.getRecentSkillComparison(userId);
        return ResponseEntity.ok(comparison);
    }

    // 🧰 8. Admin/debug — get all analytics entries
    @GetMapping
    public ResponseEntity<List<AnalyticsDTO>> getAllAnalytics() {
        List<AnalyticsDTO> all = analyticsService.getAllAnalytics();
        return ResponseEntity.ok(all);
    }
}
