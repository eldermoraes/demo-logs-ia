package com.eldermoraes.service;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class LogFileService {

    private static final Logger LOG = Logger.getLogger(LogFileService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Path LOG_DIR = Path.of("src/main/resources/logs");

    private BufferedWriter writer;

    void onStart(@Observes StartupEvent ev) {
        try {
            Files.createDirectories(LOG_DIR);
            String filename = "log_" + LocalDateTime.now().format(FMT) + ".log";
            writer = Files.newBufferedWriter(LOG_DIR.resolve(filename), StandardOpenOption.CREATE);
            LOG.infof("Log file created: %s", filename);
        } catch (IOException e) {
            LOG.errorf("Failed to create log file: %s", e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (writer != null) {
            try { writer.close(); } catch (IOException e) {
                LOG.errorf("Failed to close log file: %s", e.getMessage());
            }
        }
    }

    public void write(String line) {
        if (writer == null) return;
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            LOG.errorf("Failed to write log entry: %s", e.getMessage());
        }
    }
}
