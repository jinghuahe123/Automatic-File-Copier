package automaticFileBackup;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class FolderCheckTask extends TimerTask {

    private final String sourceFolderPath;
    private final String destinationFolderPath;
    private final StandardCopyOption copyOption;
    private final TrayIcon trayIcon;
    private final AtomicLong totalSize = new AtomicLong(0);
    private final AtomicLong copiedSize = new AtomicLong(0);
    private long startTime;

    public FolderCheckTask(String sourceFolderPath, String destinationFolderPath, StandardCopyOption copyOption, TrayIcon trayIcon) {
        this.sourceFolderPath = sourceFolderPath;
        this.destinationFolderPath = destinationFolderPath;
        this.copyOption = copyOption;
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);

        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            File[] files = sourceFolder.listFiles();
            if (files != null && files.length > 0) {
                totalSize.set(calculateTotalSize(files));
                copiedSize.set(0);

                startTime = System.currentTimeMillis();

                try {
                    for (File file : files) {
                        processFileOrDirectory(file, destinationFolder);
                    }

                    // Delete empty folders after all files have been copied, excluding the root folder
                    deleteEmptyFolders(sourceFolder, sourceFolder);

                } catch (IOException e) {
                    System.err.println("Error copying files: " + e.getMessage());
                    trayIcon.displayMessage("automaticFileBackup", "Error copying files: " + e.getMessage(), TrayIcon.MessageType.ERROR);
                }
            } else {
                //trayIcon.setToolTip("No files found to copy.");
            }
        } else {
            trayIcon.setToolTip("Source folder does not exist or is not a directory.");
        }
    }

    private void processFileOrDirectory(File file, File destinationFolder) throws IOException {
        if (file.isDirectory()) {
            // Create corresponding folder in the destination folder
            File destDir = new File(destinationFolder, file.getName());
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            // Recursively process files in the subdirectory
            FolderCheckTask task = new FolderCheckTask(file.getAbsolutePath(), destDir.getAbsolutePath(), copyOption, trayIcon);
            task.run();
        } else {
            copyFile(file, destinationFolder);
        }
    }

    private void copyFile(File file, File destinationFolder) throws IOException {
        String currentFileName = file.getName();
        File destFile = new File(destinationFolder, currentFileName);
        long fileSize = file.length();

        try (FileInputStream in = new FileInputStream(file);
             FileOutputStream out = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer
            int bytesRead;
            long currentFileCopiedSize = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                currentFileCopiedSize += bytesRead;
                copiedSize.addAndGet(bytesRead);

                // Calculate progress
                double fileProgressPercent = (currentFileCopiedSize * 100.0) / fileSize;
                double totalProgressPercent = (copiedSize.get() * 100.0) / totalSize.get();
                long elapsedTime = System.currentTimeMillis() - startTime;
                double speed = (copiedSize.get() / (1024.0 * 1024.0)) / (elapsedTime / 1000.0); // Speed in MB/s

                // Calculate elapsed time (in seconds)
                long elapsedSeconds = elapsedTime / 1000;
                long elapsedMinutes = elapsedSeconds / 60;
                long elapsedHours = elapsedMinutes / 60;

                // Estimate remaining time
                double remainingSize = totalSize.get() - copiedSize.get();
                double remainingTimeSeconds = remainingSize / (1024.0 * 1024.0 * speed); // In seconds
                long remainingMinutes = (long) (remainingTimeSeconds / 60);
                long remainingSeconds = (long) (remainingTimeSeconds % 60);

                // Update tray tooltip
                String tooltip = String.format(
                        "Copying: %s (%.2f MB/s)\nFile: %.2f%% complete | Total: %.2f%% complete\n" +
                                "Elapsed: %02d:%02d:%02d | ETA: %02d:%02d:%02d\n",
                        currentFileName, speed, fileProgressPercent, totalProgressPercent,
                        elapsedHours, elapsedMinutes % 60, elapsedSeconds % 60,
                        remainingMinutes, remainingSeconds, 0 // No seconds for the remaining time
                );
                trayIcon.setToolTip(tooltip);
            }
        }

        // Delete original file after successful copy
        if (!file.delete()) {
            System.err.println("Failed to delete original file: " + currentFileName);
        }
    }

    private long calculateTotalSize(File[] files) {
        long size = 0;
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    // This will be used for deleting empty folders recursively
    private void deleteEmptyFolders(File directory, File rootFolder) {
        // Recurse into subdirectories first
        File[] subFiles = directory.listFiles();
        if (subFiles != null) {
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    deleteEmptyFolders(subFile, rootFolder); // Recurse into subdirectories
                }
            }
        }

        // After processing subdirectories, check if the directory is empty
        // Skip deletion if the current directory is the root folder
        if (directory.equals(rootFolder)) {
            return;
        } else if (directory.isDirectory() && directory.listFiles().length == 0) {
            if (directory.delete()) {
                System.out.println("Deleted empty folder: " + directory.getAbsolutePath());
            }
        }
    }
}
