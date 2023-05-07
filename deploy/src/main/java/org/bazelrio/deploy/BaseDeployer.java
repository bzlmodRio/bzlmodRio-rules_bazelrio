package org.bazelrio.deploy;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import net.schmizz.sshj.xfer.FileSystemFile;
import java.io.IOException;
import java.io.File;

public abstract class BaseDeployer {

    protected final boolean verbose;

    protected BaseDeployer(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean establishSession(int teamNumber) {
        String[] addresses = {
            String.format("roborio-%d-frc.local", teamNumber),
            String.format("10.%d.%d.2", teamNumber / 100, teamNumber % 100),
            "172.22.11.2",
            String.format("roborio-%d-frc", teamNumber),
            String.format("roborio-%d-frc.lan", teamNumber),
            String.format("roborio-%d-frc.frc-field.local", teamNumber),
        };

        ProgressBar progressBar = new ProgressBarBuilder()
                                    .setTaskName("roboRIO Search")
                                    .setInitialMax(addresses.length)
                                    .setStyle(ProgressBarStyle.ASCII)
                                    .setUpdateIntervalMillis(100)
                                    .build();
        progressBar.stepTo(0);

        for (String address : addresses) {
            progressBar.setExtraMessage(address);
            progressBar.step();
            if (verbose) {
                System.out.println(
                    String.format("Attempting to connect to %s", address));
            }
            if (attemptConnection(address)) {
                return true;
            }

            progressBar.stepTo(addresses.length);
            progressBar.close();
        }

        progressBar.close();
        return false;
    }

    public abstract void runCommand(String command) throws IOException;

    public void copyFile(File source, String destination) {
        copyFile(new FileSystemFile(source), destination);
    }

    public abstract void copyFile(FileSystemFile source, String destination);
    
    protected abstract boolean attemptConnection(String address);
}