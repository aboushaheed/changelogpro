package com.changelogpro.services;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Service for logging plugin operations.
 * Provides both IDE logging and UI log panel updates.
 */
public class LogService {
    
    private static final Logger LOG = Logger.getInstance(LogService.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_LOG_ENTRIES = 500;
    
    private final Project project;
    private final List<LogEntry> logEntries;
    private final List<Consumer<LogEntry>> listeners;
    
    public LogService(@NotNull Project project) {
        this.project = project;
        this.logEntries = new CopyOnWriteArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Log an info message.
     */
    public void info(@NotNull String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * Log a success message.
     */
    public void success(@NotNull String message) {
        log(LogLevel.SUCCESS, message);
    }
    
    /**
     * Log a warning message.
     */
    public void warn(@NotNull String message) {
        log(LogLevel.WARNING, message);
    }
    
    /**
     * Log an error message.
     */
    public void error(@NotNull String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * Log a Git operation message.
     */
    public void git(@NotNull String message) {
        log(LogLevel.GIT, message);
    }
    
    /**
     * Log a message with the specified level.
     */
    public void log(@NotNull LogLevel level, @NotNull String message) {
        LogEntry entry = new LogEntry(level, message);
        
        // Add to internal list
        logEntries.add(entry);
        
        // Trim if too many entries
        while (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.remove(0);
        }
        
        // Log to IDE
        switch (level) {
            case ERROR:
                LOG.error(message);
                break;
            case WARNING:
                LOG.warn(message);
                break;
            default:
                LOG.info(message);
                break;
        }
        
        // Notify listeners
        for (Consumer<LogEntry> listener : listeners) {
            try {
                listener.accept(entry);
            } catch (Exception e) {
                LOG.error("Error notifying log listener", e);
            }
        }
    }
    
    /**
     * Add a listener for log entries.
     */
    public void addListener(@NotNull Consumer<LogEntry> listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener.
     */
    public void removeListener(@NotNull Consumer<LogEntry> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get all log entries.
     */
    @NotNull
    public List<LogEntry> getLogEntries() {
        return new ArrayList<>(logEntries);
    }
    
    /**
     * Clear all log entries.
     */
    public void clear() {
        logEntries.clear();
    }
    
    /**
     * Log levels.
     */
    public enum LogLevel {
        INFO("INFO", "#AAAAAA"),
        SUCCESS("SUCCESS", "#4CAF50"),
        WARNING("WARNING", "#FF9800"),
        ERROR("ERROR", "#F44336"),
        GIT("GIT", "#9C27B0");
        
        private final String label;
        private final String color;
        
        LogLevel(String label, String color) {
            this.label = label;
            this.color = color;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * A single log entry.
     */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final LogLevel level;
        private final String message;
        
        public LogEntry(@NotNull LogLevel level, @NotNull String message) {
            this.timestamp = LocalDateTime.now();
            this.level = level;
            this.message = message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getFormattedTime() {
            return timestamp.format(TIME_FORMAT);
        }
        
        public LogLevel getLevel() {
            return level;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] [%s] %s", getFormattedTime(), level.getLabel(), message);
        }
    }
}
