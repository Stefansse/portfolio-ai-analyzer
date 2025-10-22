package com.example.analyticsservice.model.mapper;

import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.model.dto.SkillProgressDTO;
import com.example.analyticsservice.model.dto.SkillProgressOverviewDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkillProgressMapper {

    public static SkillProgressOverviewDTO toOverview(List<Analytics> analyticsList) {
        SkillProgressOverviewDTO overview = new SkillProgressOverviewDTO();

        if (analyticsList == null || analyticsList.isEmpty()) {
            return overview;
        }

        overview.setUserId(analyticsList.get(0).getUserId());

        Map<String, List<SkillProgressDTO>> skillTrends = new HashMap<>();

        for (Analytics analytics : analyticsList) {
            LocalDateTime date = analytics.getUploadedAt();

            // --- Process strong skills ---
            if (analytics.getStrongSkills() != null) {
                for (String skillEntry : analytics.getStrongSkills()) {
                    String[] skills = skillEntry.split(",");
                    for (String skill : skills) {
                        skill = skill.trim();
                        if (!skill.isEmpty()) {
                            int score = getRandomScore(true); // strong skill
                            skillTrends
                                    .computeIfAbsent(skill, k -> new ArrayList<>())
                                    .add(new SkillProgressDTO(skill, score, date));
                        }
                    }
                }
            }

            // --- Process weak skills ---
            if (analytics.getWeakSkills() != null) {
                for (String skillEntry : analytics.getWeakSkills()) {
                    String[] skills = skillEntry.split(",");
                    for (String skill : skills) {
                        skill = skill.trim();
                        if (!skill.isEmpty()) {
                            int score = getRandomScore(false); // weak skill
                            skillTrends
                                    .computeIfAbsent(skill, k -> new ArrayList<>())
                                    .add(new SkillProgressDTO(skill, score, date));
                        }
                    }
                }
            }
        }

        overview.setSkillTrends(skillTrends);
        return overview;
    }

    /**
     * Returns a random score based on skill type
     * Strong skills: 7-10
     * Weak skills: 1-5
     */
    private static int getRandomScore(boolean isStrong) {
        if (isStrong) {
            return ThreadLocalRandom.current().nextInt(7, 11); // 7-10
        } else {
            return ThreadLocalRandom.current().nextInt(1, 6);  // 1-5
        }
    }
}
