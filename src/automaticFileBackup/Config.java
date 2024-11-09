package automaticFileBackup;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class Config {

    private final String sourceFolder;
    private final String destinationFolder;
    private final long pollingInterval;
    private final StandardCopyOption copyOption;

    private Config(String sourceFolder, String destinationFolder, long pollingInterval, StandardCopyOption copyOption) {
        this.sourceFolder = sourceFolder;
        this.destinationFolder = destinationFolder;
        this.pollingInterval = pollingInterval;
        this.copyOption = copyOption;
    }

    public static Config load(String filePath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(filePath));

        String sourceFolder = properties.getProperty("sourceFolder");
        String destinationFolder = properties.getProperty("destinationFolder");
        long pollingInterval = Long.parseLong(properties.getProperty("pollingInterval"));
        StandardCopyOption copyOption = StandardCopyOption.valueOf(properties.getProperty("copyOption"));

        return new Config(sourceFolder, destinationFolder, pollingInterval, copyOption);
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public StandardCopyOption getCopyOption() {
        return copyOption;
    }
}
