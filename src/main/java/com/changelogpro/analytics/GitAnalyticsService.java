package com.changelogpro.analytics;

import com.changelogpro.analytics.model.CommitInfo;
import com.changelogpro.analytics.model.ContributorStats;
import com.changelogpro.services.LogService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for analyzing Git repository history.
 * Works with any Git provider by using Git CLI commands directly.
 * 
 * <p>This service provides methods to:
 * <ul>
 *     <li>Retrieve commit history with metadata</li>
 *     <li>Parse conventional commit messages</li>
 *     <li>Calculate contributor statistics</li>
 *     <li>Get tag and release information</li>
 * </ul>
 */
public class GitAnalyticsService {
    
    // Conventional Commits pattern: type(scope)!: description
    private static final Pattern CONVENTIONAL_COMMIT_PATTERN = Pattern.compile(
        "^(?<type>feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)" +
        "(?:\\((?<scope>[^)]+)\\))?" +
        "(?<breaking>!)?" +
        ":\\s*(?<description>.+)$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Issue reference patterns
    private static final Pattern GITHUB_ISSUE_PATTERN = Pattern.compile("#(\\d+)");
    private static final Pattern JIRA_ISSUE_PATTERN = Pattern.compile("([A-Z][A-Z0-9]+-\\d+)");
    
    // Git log format: hash|author|email|date|subject|parents
    private static final String LOG_FORMAT = "%H|%an|%ae|%aI|%s|%P";
    
    private final Project project;
    private final LogService logger;
    
    public GitAnalyticsService(@NotNull Project project, @NotNull LogService logger) {
        this.project = project;
        this.logger = logger;
    }
    
    /**
     * Checks if the project is a Git repository.
     */
    public boolean isGitRepository() {
        String result = executeGitCommand("rev-parse", "--is-inside-work-tree");
        return result != null && result.trim().equals("true");
    }
    
    /**
     * Gets the list of tags in the repository, sorted by date (newest first).
     */
    @NotNull
    public List<String> getTags() {
        String result = executeGitCommand("tag", "--sort=-creatordate");
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(result.split("\n"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
    
    /**
     * Gets the total number of commits in the repository.
     */
    public int getTotalCommitCount() {
        String result = executeGitCommand("rev-list", "--count", "HEAD");
        if (result == null) return 0;
        
        try {
            return Integer.parseInt(result.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Gets commits between two references (tags, branches, or commits).
     * 
     * @param fromRef Starting reference (exclusive), null for beginning of history
     * @param toRef Ending reference (inclusive), defaults to HEAD
     * @param limit Maximum number of commits to return, -1 for all
     * @return List of commits in reverse chronological order
     */
    @NotNull
    public List<CommitInfo> getCommits(@Nullable String fromRef, @Nullable String toRef, int limit) {
        List<CommitInfo> commits = new ArrayList<>();
        
        List<String> args = new ArrayList<>();
        args.add("log");
        args.add("--pretty=format:" + LOG_FORMAT);
        
        if (limit > 0) {
            args.add("-n");
            args.add(String.valueOf(limit));
        }
        
        // Build range
        String range;
        if (fromRef != null && !fromRef.isEmpty()) {
            range = fromRef + ".." + (toRef != null ? toRef : "HEAD");
        } else {
            range = toRef != null ? toRef : "HEAD";
        }
        args.add(range);
        
        String result = executeGitCommand(args.toArray(new String[0]));
        if (result == null || result.isEmpty()) {
            return commits;
        }
        
        for (String line : result.split("\n")) {
            CommitInfo commit = parseCommitLine(line.trim());
            if (commit != null) {
                commits.add(commit);
            }
        }
        
        return commits;
    }
    
    /**
     * Gets the most recent commits.
     */
    @NotNull
    public List<CommitInfo> getRecentCommits(int limit) {
        return getCommits(null, "HEAD", limit);
    }
    
    /**
     * Gets commits since the last tag.
     */
    @NotNull
    public List<CommitInfo> getCommitsSinceLastTag() {
        List<String> tags = getTags();
        if (tags.isEmpty()) {
            return getCommits(null, "HEAD", -1);
        }
        return getCommits(tags.get(0), "HEAD", -1);
    }
    
    /**
     * Calculates contributor statistics from commit history.
     * 
     * @param commits List of commits to analyze
     * @return Map of contributor name to their stats
     */
    @NotNull
    public Map<String, ContributorStats> calculateContributorStats(@NotNull List<CommitInfo> commits) {
        Map<String, ContributorStats> statsMap = new LinkedHashMap<>();
        
        for (CommitInfo commit : commits) {
            String author = commit.getAuthor();
            String email = commit.getAuthorEmail();
            
            ContributorStats stats = statsMap.computeIfAbsent(author, 
                name -> new ContributorStats(name, email));
            
            LocalDate date = commit.getDateTime().toLocalDate();
            stats.addCommit(date, commit.getConventionalType());
        }
        
        return statsMap;
    }
    
    /**
     * Groups commits by month.
     * 
     * @param commits List of commits to group
     * @return Map of month string (YYYY-MM) to commit count
     */
    @NotNull
    public Map<String, Integer> groupCommitsByMonth(@NotNull List<CommitInfo> commits) {
        Map<String, Integer> byMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (CommitInfo commit : commits) {
            String month = commit.getDateTime().format(formatter);
            byMonth.merge(month, 1, Integer::sum);
        }
        
        return byMonth;
    }
    
    /**
     * Counts commits that follow Conventional Commits format.
     */
    public int countConventionalCommits(@NotNull List<CommitInfo> commits) {
        return (int) commits.stream()
            .filter(CommitInfo::isConventionalCommit)
            .count();
    }
    
    /**
     * Gets the date of a tag.
     */
    @Nullable
    public LocalDate getTagDate(@NotNull String tagName) {
        // Try to get tag creation date
        String result = executeGitCommand("log", "-1", "--format=%aI", tagName);
        if (result == null || result.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(result.trim(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parses a single commit line from git log output.
     */
    @Nullable
    private CommitInfo parseCommitLine(@NotNull String line) {
        if (line.isEmpty()) return null;
        
        String[] parts = line.split("\\|", 6);
        if (parts.length < 5) return null;
        
        try {
            String hash = parts[0];
            String author = parts[1];
            String email = parts[2];
            String dateStr = parts[3];
            String subject = parts[4];
            String parents = parts.length > 5 ? parts[5] : "";
            
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            boolean isMerge = parents.contains(" "); // Multiple parents = merge commit
            
            CommitInfo.Builder builder = CommitInfo.builder()
                .hash(hash)
                .author(author)
                .authorEmail(email)
                .dateTime(dateTime)
                .subject(subject)
                .mergeCommit(isMerge);
            
            // Parse conventional commit
            parseConventionalCommit(subject, builder);
            
            // Extract issue reference
            extractIssueReference(subject, builder);
            
            return builder.build();
            
        } catch (Exception e) {
            logger.warn("Failed to parse commit line: " + line);
            return null;
        }
    }
    
    /**
     * Parses conventional commit format from subject.
     */
    private void parseConventionalCommit(@NotNull String subject, @NotNull CommitInfo.Builder builder) {
        Matcher matcher = CONVENTIONAL_COMMIT_PATTERN.matcher(subject);
        if (matcher.matches()) {
            builder.conventionalType(matcher.group("type").toLowerCase());
            
            String scope = matcher.group("scope");
            if (scope != null) {
                builder.conventionalScope(scope);
            }
            
            boolean breaking = matcher.group("breaking") != null;
            builder.breakingChange(breaking);
        }
    }
    
    /**
     * Extracts issue reference from commit message.
     */
    private void extractIssueReference(@NotNull String subject, @NotNull CommitInfo.Builder builder) {
        // Try JIRA pattern first (more specific)
        Matcher jiraMatcher = JIRA_ISSUE_PATTERN.matcher(subject);
        if (jiraMatcher.find()) {
            builder.issueReference(jiraMatcher.group(1));
            return;
        }
        
        // Try GitHub pattern
        Matcher githubMatcher = GITHUB_ISSUE_PATTERN.matcher(subject);
        if (githubMatcher.find()) {
            builder.issueReference("#" + githubMatcher.group(1));
        }
    }
    
    /**
     * Executes a Git command and returns the output.
     */
    @Nullable
    private String executeGitCommand(String... args) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        
        try {
            String[] command = new String[args.length + 1];
            command[0] = "git";
            System.arraycopy(args, 0, command, 1, args.length);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(basePath));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logger.error("Git command timed out");
                return null;
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Don't log errors for expected failures (e.g., no tags)
                return null;
            }
            
            return output.toString().trim();
            
        } catch (Exception e) {
            logger.error("Git command failed: " + e.getMessage());
            return null;
        }
    }
}
