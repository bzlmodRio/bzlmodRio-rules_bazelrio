package org.bazelrio.deploy;

import com.google.devtools.build.runfiles.Runfiles;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
// import net.schmizz.sshj.SSHClient;
// import net.schmizz.sshj.connection.channel.direct.Session;
// import net.schmizz.sshj.connection.channel.direct.Session.Command;
// import net.schmizz.sshj.transport.verification.HostKeyVerifier;
// import net.schmizz.sshj.xfer.FileSystemFile;
// import net.schmizz.sshj.xfer.scp.SCPUploadClient;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;


class Deploy {
  public static void main(String[] args) throws IOException {
    System.out.println("Starting deploy");

    ArgumentParser parser =
        ArgumentParsers.newFor("deploy").build().description(
            "Deploy code to a roboRIO");
    parser.addArgument("--robot_binary")
        .type(String.class)
        .help("the executable binary to deploy")
        .required(true);
    parser.addArgument("--robot_command")
        .type(String.class)
        .help(
            "the script to run the robot binary (deployed to /home/lvuser/robotCommand)")
        .setDefault("{}");
    parser.addArgument("--team_number")
        .type(Integer.class)
        .help("the team number, used for the roboRIO addresses")
        .required(true);
    parser.addArgument("--verbose")
        .action(Arguments.storeTrue())
        .setDefault(false);
    parser.addArgument("--dynamic_libraries")
        .type(String.class)
        .nargs("*")
        .help("the .so libraries which the executable requires")
        .setDefault();
    parser.addArgument("--skip_dynamic_libraries")
        .action(Arguments.storeTrue())
        .help(
            "don't deploy the dynamic libraries (warning: unsafe, might lead to unexpected issues)")
        .setDefault(false);

    Namespace parsedArgs = parser.parseArgsOrFail(args);

    Runfiles runfiles = Runfiles.create();

    File robotBinary = runfile(runfiles, parsedArgs.get("robot_binary"));
    String robotCommand = parsedArgs.get("robot_command");
    ArrayList<String> dynamicLibraryPaths = parsedArgs.get("dynamic_libraries");
    boolean verbose = parsedArgs.get("verbose");

    if (parsedArgs.get("skip_dynamic_libraries")) {
      if (verbose) {
        System.out.println("Skipping dynamic libraries");
      }
      dynamicLibraryPaths = new ArrayList<>();
    }

    String robotBinaryDestination =
        String.format("/home/lvuser/%s", robotBinary.getName());

    ProgressBar progressBar = new ProgressBarBuilder()
                                  .setTaskName("Deploying")
                                  .setInitialMax(dynamicLibraryPaths.size() + 1)
                                  .setStyle(ProgressBarStyle.ASCII)
                                  .setUpdateIntervalMillis(100)
                                  .build();
    progressBar.stepTo(0);

    BaseDeployer deployer = new DryRunDeployer(verbose);

    if (!deployer.establishSession(parsedArgs.get("team_number"))) {
      System.err.println("Couldn't find a roboRIO");
      System.exit(-1);
    }

    // Stop and remove existing robot binary
    deployer.runCommand(". /etc/profile.d/natinst-path.sh; /usr/local/frc/bin/frcKillRobot.sh -t");
    deployer.runCommand(String.format("rm -f %s", robotBinaryDestination));

    // Fix some files (required only once after roboRIO format)
    deployer.runCommand("sed -i -e 's/\\\"exec /\\\"/' /usr/local/frc/bin/frcRunRobot.sh");
    deployer.runCommand("sed -i -e 's/^StartupDLLs/;StartupDLLs/' /etc/natinst/share/ni-rt.ini");

    // Copy new robot binary
    progressBar.setExtraMessage(robotBinary.getName());
    deployer.copyFile(robotBinary, robotBinaryDestination);
    deployer.runCommand(String.format("chmod +x %s", robotBinaryDestination));
    deployer.runCommand(String.format("chown lvuser:ni %s", robotBinaryDestination));
    deployer.runCommand(String.format("setcap cap_sys_nice+eip %s", robotBinaryDestination));
    progressBar.step();

    // Write new robotCommand
    deployer.runCommand(
        String.format("echo %s > /home/lvuser/robotCommand",
                      robotCommand.replace("{}", robotBinaryDestination)));
    deployer.runCommand("chmod +x /home/lvuser/robotCommand");
    deployer.runCommand("chown lvuser:ni /home/lvuser/robotCommand");

    // Copy dynamic libraries
    for (String dynamicLibraryPath : dynamicLibraryPaths) {
      File dynamicLibrary = runfile(runfiles, dynamicLibraryPath);
      progressBar.setExtraMessage("Deploying " + dynamicLibrary.getName());
      String dynamicLibraryDestination = String.format(
          "/usr/local/frc/third-party/lib/%s", dynamicLibrary.getName());
      deployer.copyFile(dynamicLibrary, dynamicLibraryDestination);
      progressBar.step();
    }
    progressBar.close();

    // Restart robot code
    System.out.print("Restarting robot code... ");
    deployer.runCommand("sync");
    deployer.runCommand("ldconfig");
    deployer.runCommand(". /etc/profile.d/natinst-path.sh; /usr/local/frc/bin/frcKillRobot.sh -t -r");
    System.out.println("Done.");
    System.out.print("Deploy completed!");
  }

  static File runfile(Runfiles runfiles, String location) {
    Path runfilePath = Path.of("__main__").resolve(location).normalize();
    ArrayList<String> pathParts = new ArrayList<>();
    runfilePath.iterator().forEachRemaining(
        part -> pathParts.add(part.toString()));
    return new File(runfiles.rlocation(String.join("/", pathParts)));
  }
}
