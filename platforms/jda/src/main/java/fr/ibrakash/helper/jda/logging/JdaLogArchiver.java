package fr.ibrakash.helper.jda.logging;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Archives previous log files into dated ZIP archives at startup.
 * Adapted from Cordzy's {@code LogArchiver}.
 */
public final class JdaLogArchiver {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) .*");

    private JdaLogArchiver() {}

    /**
     * Scans the {@code logs/} directory next to the working directory,
     * renames un-dated log files to a timestamped name and zips them.
     */
    public static void archiveLatestLogs() throws IOException {
        archiveLatestLogs(Paths.get("logs"));
    }

    public static void archiveLatestLogs(Path logsDir) throws IOException {
        if (!Files.exists(logsDir) || !Files.isDirectory(logsDir)) return;

        Files.walkFileTree(logsDir, new SimpleFileVisitor<>() {
            @NotNull
            @Override
            public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
                String filename = file.getFileName().toString();
                boolean isLogFile = (filename.endsWith(".txt") || filename.endsWith(".log"))
                        && !filename.matches("^\\d{4}-\\d{2}-\\d{2}.*\\.log$");

                if (!isLogFile) return FileVisitResult.CONTINUE;

                try {
                    List<String> lines = Files.readAllLines(file);
                    if (lines.isEmpty()) return FileVisitResult.CONTINUE;

                    Matcher matcher = DATE_PATTERN.matcher(lines.getFirst());
                    if (!matcher.matches()) return FileVisitResult.CONTINUE;

                    String datePart = matcher.group(1);
                    String newName  = datePart + "_" + matcher.group(2) + "_" + matcher.group(3) + "_" + matcher.group(4) + ".log";
                    Path renamed    = logsDir.resolve(newName);

                    Files.move(file, renamed, StandardCopyOption.REPLACE_EXISTING);

                    Path zipFile = logsDir.resolve(newName.replace(".log", ".zip"));
                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
                        zos.setLevel(9);
                        zos.putNextEntry(new ZipEntry(newName));
                        Files.copy(renamed, zos);
                        zos.closeEntry();
                    }
                    Files.deleteIfExists(renamed);
                } catch (Exception e) {
                    System.err.println("[JdaLogArchiver] Error processing " + file + ": " + e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

