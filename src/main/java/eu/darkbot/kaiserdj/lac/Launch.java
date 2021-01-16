package eu.darkbot.kaiserdj.lac;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.tree.components.JFileOpener;
import com.github.manolo8.darkbot.utils.AuthAPI;
import com.github.manolo8.darkbot.utils.SystemUtils;

import java.nio.file.Files;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

@Feature(name = "Launch", description = "Open the account you are using at that moment in DarkOrbit Client (with Dosid)")
public class Launch implements
        Module,
        InstructionProvider,
        Configurable<Launch.Config>,
        ExtraMenuProvider {

    private Main main;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        this.main = main;
    }

    @Override
    public boolean canRefresh() {
        return true;
    }

    @Override
    public void tick() {
    }

    public JComponent beforeConfig() {
        final JButton tutorial = new JButton("Tutorial");
        tutorial.addActionListener(e -> {
            JButton github = new JButton("Download client");
            github.addActionListener(x -> SystemUtils.openUrl("https://github.com/kaiserdj/Darkorbit-client/releases/latest"));

            JButton detectClient = new JButton("Automatic client detection");
            detectClient.addActionListener((a -> {
                String client = System.getenv("LOCALAPPDATA") + "\\Programs\\darkorbit-client\\DarkOrbit Client.exe";

                if (Files.exists(Paths.get(client))) {
                    this.config.CUSTOM_FILE = client;
                    Popups.showMessageAsync("Client detected", "The client has been detected and the location saved.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Popups.showMessageAsync("Error", "The client has not been detected.\n" +
                            "Please check that you have the client installed and try again.\n" +
                            "If you keep getting error, please enter the client's .exe location manually", JOptionPane.ERROR_MESSAGE);
                }
            }));

            JButton video = new JButton("Video-tutorial");
            video.addActionListener(x -> SystemUtils.openUrl("https://vimeo.com/501184515"));

            Object[] options = {
                    "Basic guide to configure the plugin:",
                    " ",
                    "Download and install the client",
                    github,
                    " ",
                    "Try to automatically detect the client's .exe location",
                    detectClient,
                    " ",
                    "Manual way to configure plugin",
                    "1º Obtain the location of the client's .exe file",
                    "2º Select in the option \"Launcher.exe\" of the plugin the file \"DarkOrbit Client.exe\"",
                    video
            };

            Popups.showMessageAsync("Tutorial", options, JOptionPane.INFORMATION_MESSAGE);

            return;
        });

        return tutorial;
    }

    public static class Config {
        @Option(value = "Launcher.exe", description = "Select the location of the launcher .exe file")
        @Editor(JFileOpener.class)
        public String CUSTOM_FILE;
    }

    private Config config;

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(Main main) {
        return Arrays.asList(
                createSeparator("Launch"),
                create("Open client", e -> {
                            String sid = this.main.statsManager.sid, instance = this.main.statsManager.instance;
                            if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) {
                                Popups.showMessageAsync(
                                        "Error",
                                        "Error getting user data",
                                        JOptionPane.INFORMATION_MESSAGE);

                                return;
                            }
                            String url = instance + "?dosid=" + sid;

                            try {
                                new ProcessBuilder(this.config.CUSTOM_FILE, "--dosid", url).start();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                ));
    }
}
