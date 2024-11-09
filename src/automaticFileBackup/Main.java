package automaticFileBackup;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        try {
            // Load configuration
            Config config = Config.load("config.properties"); //C:\Users\cityo\IdeaProjects\Automatic File Copier\src\

            // Initialize system tray icon
            if (!SystemTray.isSupported()) {
                System.err.println("System tray not supported on this platform.");
                return;
            }
            SystemTray tray = SystemTray.getSystemTray();

            // Load the tray icon from resources folder
            Image image = loadTrayIconImage();
            if (image == null) {
                System.err.println("Tray icon not found!");
                return;
            }

            TrayIcon trayIcon = new TrayIcon(image, "automaticFileBackup");
            trayIcon.setImageAutoSize(true);

            // Add exit action to the tray icon
            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);

            // Set up and start the FolderCheckTask
            Timer timer = new Timer();
            FolderCheckTask task = new FolderCheckTask(
                    config.getSourceFolder(),
                    config.getDestinationFolder(),
                    config.getCopyOption(),
                    trayIcon
            );
            timer.schedule(task, 0, config.getPollingInterval());

            //trayIcon.displayMessage("Folder Poller", "Started monitoring for file changes.", TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load tray icon image from the resources folder
    private static Image loadTrayIconImage() {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("icon.png")) {
            if (inputStream == null) {
                System.out.println("Icon file not found in resources.");
                return null;
            }
            return Toolkit.getDefaultToolkit().createImage(inputStream.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
