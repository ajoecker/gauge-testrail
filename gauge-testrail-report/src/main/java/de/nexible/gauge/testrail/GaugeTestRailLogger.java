package de.nexible.gauge.testrail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GaugeTestRailLogger {
    public static void initializeLogger() {
        String root = System.getenv("GAUGE_PROJECT_ROOT");
        String reportsDir = System.getenv("logs_directory");
        Path logFile = Paths.get(root, reportsDir, "testrail.log");
        try {
            FileHandler fileHandler = new FileHandler(logFile.toString(), 0, 1, false);
            CustomLogFormatter customLogFormatter = new CustomLogFormatter();
            fileHandler.setFormatter(customLogFormatter);
            fileHandler.setLevel(Level.FINE);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(customLogFormatter);
            consoleHandler.setLevel(Level.FINE);

            Logger logger = LogManager.getLogManager().getLogger("");
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);

            Logger.getLogger(GaugeTestRailLogger.class.getName()).info(() -> "Logging initialized. Logging into " + logFile);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger, due to:");
            e.printStackTrace();
        }
    }

    static class CustomLogFormatter extends Formatter {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss.SSS");

        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(1000);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), ZoneId.systemDefault());
            builder.append(zdt.format(FORMATTER)).append(" - ");
            builder.append("[").append(record.getSourceClassName()).append(".");
            builder.append(record.getSourceMethodName()).append("] - ");
            builder.append("[").append(record.getLevel()).append("] - ");
            builder.append(formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
