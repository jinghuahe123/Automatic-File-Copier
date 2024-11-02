import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.TimerTask;

public class FolderCheckTask extends TimerTask {

    private final String sourceFolderPath;
    private final String destinationFolderPath;
    private final StandardCopyOption copyOption;

    public FolderCheckTask(String sourceFolderPath, String destinationFolderPath, StandardCopyOption copyOption) {
        this.sourceFolderPath = sourceFolderPath;
        this.destinationFolderPath = destinationFolderPath;
        this.copyOption = copyOption;
    }

    @Override
    public void run() {
        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);

        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            File[] files = sourceFolder.listFiles();
            if (files != null && files.length > 0) {
                System.out.println("Files found in the source folder:");

                // Set folder to read-only
                sourceFolder.setWritable(false);
                try {
                    for (File file : files) {
                        if (file.isFile()) {
                            System.out.println("Copying file: " + file.getName());
                            File destFile = new File(destinationFolder, file.getName());

                            // Copy file to destination folder with specified copy option
                            Files.copy(file.toPath(), destFile.toPath(), copyOption);

                            // Delete the original file after a successful copy
                            if (file.delete()) {
                                System.out.println("Deleted original file: " + file.getName());
                            } else {
                                System.err.println("Failed to delete original file: " + file.getName());
                            }
                        }
                    }
                    System.out.println("All files copied and original files deleted successfully.");
                } catch (IOException e) {
                    System.err.println("Error copying files: " + e.getMessage());
                } finally {
                    // Set folder back to writable
                    sourceFolder.setWritable(true);
                }
            } else {
                System.out.println("No files found in the source folder.");
            }
        } else {
            System.out.println("Source folder does not exist or is not a directory.");
        }
    }
}
