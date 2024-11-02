import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Timer;

public class FolderPoller {

    public static void main(String[] args) {
        Properties config = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            // Load properties from the configuration file
            config.load(input);

            String sourceFolderPath = config.getProperty("sourceFolderPath");
            String destinationFolderPath = config.getProperty("destinationFolderPath");
            int pollIntervalMs = Integer.parseInt(config.getProperty("pollIntervalMs"));
            StandardCopyOption copyOption = StandardCopyOption.valueOf(config.getProperty("copyOption"));

            // Schedule FolderCheckTask with loaded configurations
            Timer timer = new Timer();
            FolderCheckTask task = new FolderCheckTask(sourceFolderPath, destinationFolderPath, copyOption);
            timer.schedule(task, 0, pollIntervalMs);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }
}
