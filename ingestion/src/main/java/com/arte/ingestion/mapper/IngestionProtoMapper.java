package com.arte.ingestion.mapper;

import com.arte.ingestion.dto.leetcode.LeetCodeStats;
import com.arte.ingestion.dto.resume.ResumeSummary;
import com.arte.ingestion.entity.UserInfo;
import com.arte.ingestion.dto.github.GitHubStats;
import com.arte.ingestion.dto.github.RepoSummary;
import com.arte.ingestion.grpc.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class IngestionProtoMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static com.arte.ingestion.grpc.GitHubStats toProto(GitHubStats stats) {
        if (stats == null) return null;

        var builder = com.arte.ingestion.grpc.GitHubStats.newBuilder()
                .setTotalStars(stats.getTotalStars() != null ? stats.getTotalStars() : 0)
                .setTotalForks(stats.getTotalForks() != null ? stats.getTotalForks() : 0)
                .setTotalPinnedRepos(stats.getTotalPinnedRepos() != null ? stats.getTotalPinnedRepos() : 0);

        if (stats.getPinnedRepos() != null) {
            stats.getPinnedRepos().forEach(repo -> builder.addPinnedRepos(toProto(repo)));
        }

        if (stats.getLanguageDistribution() != null) {
            builder.putAllLanguageDistribution(stats.getLanguageDistribution());
        }

        if (stats.getTopTopics() != null) {
            builder.addAllTopTopics(stats.getTopTopics());
        }

        if (stats.getLastSynced() != null) {
            builder.setLastSynced(stats.getLastSynced().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return builder.build();
    }

    public static com.arte.ingestion.grpc.RepoSummary toProto(RepoSummary repo) {
        if (repo == null) return null;

        return com.arte.ingestion.grpc.RepoSummary.newBuilder()
                .setName(repo.getName() != null ? repo.getName() : "")
                .setUrl(repo.getUrl() != null ? repo.getUrl() : "")
                .setStars(repo.getStars() != null ? repo.getStars() : 0)
                .setForks(repo.getForks() != null ? repo.getForks() : 0)
                .setPrimaryLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage() : "")
                .build();
    }

    public static com.arte.ingestion.grpc.LeetCodeStats toProto(LeetCodeStats stats) {
        if (stats == null) return null;

        var builder = com.arte.ingestion.grpc.LeetCodeStats.newBuilder()
                .setUsername(stats.getUsername() != null ? stats.getUsername() : "")
                .setTotalSolved(stats.getTotalSolved() != null ? stats.getTotalSolved() : 0)
                .setEasySolved(stats.getEasySolved() != null ? stats.getEasySolved() : 0)
                .setMediumSolved(stats.getMediumSolved() != null ? stats.getMediumSolved() : 0)
                .setHardSolved(stats.getHardSolved() != null ? stats.getHardSolved() : 0)
                .setRanking(stats.getRanking() != null ? stats.getRanking() : 0)
                .setReputation(stats.getReputation() != null ? stats.getReputation() : 0)
                .setStarRating(stats.getStarRating() != null ? stats.getStarRating() : 0.0)
                .setAboutMe(stats.getAboutMe() != null ? stats.getAboutMe() : "");

        if (stats.getBadges() != null) {
            builder.addAllBadges(stats.getBadges());
        }

        if (stats.getActiveBadge() != null) {
            builder.setActiveBadge(stats.getActiveBadge());
        }

        builder.setContestsAttended(stats.getContestsAttended() != null ? stats.getContestsAttended() : 0)
                .setContestRating(stats.getContestRating() != null ? stats.getContestRating() : 0.0)
                .setGlobalRanking(stats.getGlobalRanking() != null ? stats.getGlobalRanking() : 0)
                .setTopPercentage(stats.getTopPercentage() != null ? stats.getTopPercentage() : 0.0);

        if (stats.getLanguageStats() != null) {
            builder.putAllLanguageStats(stats.getLanguageStats());
        }

        if (stats.getRecentSubmissions() != null) {
            stats.getRecentSubmissions().forEach(sub -> builder.addRecentSubmissions(toProto(sub)));
        }

        return builder.build();
    }

    public static com.arte.ingestion.grpc.RecentSubmission toProto(LeetCodeStats.RecentSubmission sub) {
        if (sub == null) return null;

        return com.arte.ingestion.grpc.RecentSubmission.newBuilder()
                .setTitle(sub.getTitle() != null ? sub.getTitle() : "")
                .setTitleSlug(sub.getTitleSlug() != null ? sub.getTitleSlug() : "")
                .setLanguage(sub.getLanguage() != null ? sub.getLanguage() : "")
                .setTimestamp(sub.getTimestamp() != null ? sub.getTimestamp() : 0L)
                .build();
    }

    public static com.arte.ingestion.grpc.ResumeSummary toProto(ResumeSummary summary) {
        if (summary == null) return null;

        var builder = com.arte.ingestion.grpc.ResumeSummary.newBuilder()
                .setFileName(summary.getFileName() != null ? summary.getFileName() : "")
                .setFileHash(summary.getFileHash() != null ? summary.getFileHash() : "")
                .setWordCount(summary.getWordCount() != null ? summary.getWordCount() : 0)
                .setRawText(summary.getRawText() != null ? summary.getRawText() : "");

        if (summary.getProcessedAt() != null) {
            builder.setProcessedAt(summary.getProcessedAt().toString());
        }

        if (summary.getSkills() != null) {
            builder.addAllSkills(summary.getSkills());
        }

        if (summary.getExperiences() != null) {
            builder.addAllExperiences(summary.getExperiences());
        }

        if (summary.getEducation() != null) {
            builder.addAllEducation(summary.getEducation());
        }

        if (summary.getSummary() != null) {
            builder.setSummary(summary.getSummary());
        }

        return builder.build();
    }

    public static UserInfoData toProto(UserInfo userInfo) {
        if (userInfo == null) return null;

        var builder = UserInfoData.newBuilder()
                .setUserId(userInfo.getUserId().toString());

        if (userInfo.getGithubStats() != null) {
            GitHubStats githubStats = mapper.convertValue(userInfo.getGithubStats(), GitHubStats.class);
            builder.setGithubStats(toProto(githubStats));
        }

        if (userInfo.getLeetcodeStats() != null) {
            LeetCodeStats leetcodeStats = mapper.convertValue(userInfo.getLeetcodeStats(), LeetCodeStats.class);
            builder.setLeetcodeStats(toProto(leetcodeStats));
        }

        if (userInfo.getResumeSummary() != null) {
            ResumeSummary resumeSummary = mapper.convertValue(userInfo.getResumeSummary(), ResumeSummary.class);
            builder.setResumeSummary(toProto(resumeSummary));
        }

        if (userInfo.getProcessingVersion() != null) {
            builder.setProcessingVersion(userInfo.getProcessingVersion());
        }

        if (userInfo.getProcessedAt() != null) {
            builder.setProcessedAt(userInfo.getProcessedAt().toString());
        }

        if (userInfo.getLastIngestedAt() != null) {
            builder.setLastIngestedAt(userInfo.getLastIngestedAt().toString());
        }

        return builder.build();
    }

    // from proto to entity (for updates from processing service)
    public static GitHubStats fromProto(com.arte.ingestion.grpc.GitHubStats proto) {
        if (proto == null) return null;

        List<RepoSummary> repos = proto.getPinnedReposList().stream()
                .map(IngestionProtoMapper::fromProto)
                .collect(Collectors.toList());

        return GitHubStats.builder()
                .totalStars(proto.getTotalStars())
                .totalForks(proto.getTotalForks())
                .totalPinnedRepos(proto.getTotalPinnedRepos())
                .pinnedRepos(repos)
                .languageDistribution(proto.getLanguageDistributionMap())
                .topTopics(proto.getTopTopicsList())
                .lastSynced(proto.getLastSynced().isEmpty() ? null : 
                        LocalDateTime.parse(proto.getLastSynced(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    public static RepoSummary fromProto(com.arte.ingestion.grpc.RepoSummary proto) {
        if (proto == null) return null;

        return RepoSummary.builder()
                .name(proto.getName())
                .url(proto.getUrl())
                .stars(proto.getStars())
                .forks(proto.getForks())
                .primaryLanguage(proto.getPrimaryLanguage())
                .build();
    }

    public static LeetCodeStats fromProto(com.arte.ingestion.grpc.LeetCodeStats proto) {
        if (proto == null) return null;

        List<LeetCodeStats.RecentSubmission> submissions = proto.getRecentSubmissionsList().stream()
                .map(IngestionProtoMapper::fromProto)
                .collect(Collectors.toList());

        return LeetCodeStats.builder()
                .username(proto.getUsername())
                .totalSolved(proto.getTotalSolved())
                .easySolved(proto.getEasySolved())
                .mediumSolved(proto.getMediumSolved())
                .hardSolved(proto.getHardSolved())
                .ranking(proto.getRanking())
                .reputation(proto.getReputation())
                .starRating(proto.getStarRating())
                .aboutMe(proto.getAboutMe())
                .badges(proto.getBadgesList())
                .activeBadge(proto.getActiveBadge().isEmpty() ? null : proto.getActiveBadge())
                .contestsAttended(proto.getContestsAttended())
                .contestRating(proto.getContestRating())
                .globalRanking(proto.getGlobalRanking())
                .topPercentage(proto.getTopPercentage())
                .languageStats(proto.getLanguageStatsMap())
                .recentSubmissions(submissions)
                .build();
    }

    public static LeetCodeStats.RecentSubmission fromProto(com.arte.ingestion.grpc.RecentSubmission proto) {
        if (proto == null) return null;

        return LeetCodeStats.RecentSubmission.builder()
                .title(proto.getTitle())
                .titleSlug(proto.getTitleSlug())
                .language(proto.getLanguage())
                .timestamp(proto.getTimestamp())
                .build();
    }

    public static ResumeSummary fromProto(com.arte.ingestion.grpc.ResumeSummary proto) {
        if (proto == null) return null;

        return ResumeSummary.builder()
                .fileName(proto.getFileName())
                .fileHash(proto.getFileHash())
                .wordCount(proto.getWordCount())
                .processedAt(proto.getProcessedAt().isEmpty() ? null : Instant.parse(proto.getProcessedAt()))
                .rawText(proto.getRawText())
                .skills(proto.getSkillsList())
                .experiences(proto.getExperiencesList())
                .education(proto.getEducationList())
                .summary(proto.getSummary())
                .build();
    }
}
