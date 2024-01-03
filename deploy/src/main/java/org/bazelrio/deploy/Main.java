package org.bazelrio.deploy;

import com.google.devtools.build.runfiles.AutoBazelRepository;
import com.google.devtools.build.runfiles.Runfiles;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

@AutoBazelRepository
final class Main {
  private Main() {}

  public static void main(String[] args) throws IOException {
    ArgumentParser parser =
        ArgumentParsers.newFor("deploy").build().description("Deploy code to a roboRIO");
    parser
        .addArgument("--robot_binary")
        .type(String.class)
        .help("the executable binary to deploy")
        .required(true);
    parser
        .addArgument("--team_number")
        .type(Integer.class)
        .help("the team number, used for the roboRIO addresses")
        .required(true);
    parser.addArgument("--verbose").action(Arguments.storeTrue()).setDefault(false);
    parser.addArgument("--dry_run").action(Arguments.storeTrue()).setDefault(false);
    parser.addArgument("--is_java").action(Arguments.storeTrue()).setDefault(false);
    parser
        .addArgument("--dynamic_libraries")
        .type(String.class)
        .nargs("*")
        .help("the .so libraries which the executable requires")
        .setDefault();
    parser
        .addArgument("--skip_dynamic_libraries")
        .action(Arguments.storeTrue())
        .help(
            "don't deploy the dynamic libraries (warning: unsafe, might lead to unexpected issues)")
        .setDefault(false);

    Namespace parsedArgs = parser.parseArgsOrFail(args);

    Runfiles runfiles = Runfiles.preload().withSourceRepository(AutoBazelRepository_Deploy.NAME);

    File robotBinary = new File(parsedArgs.get("robot_binary").toString());
    ArrayList<String> dynamicLibraryPaths = parsedArgs.get("dynamic_libraries");
    boolean verbose = parsedArgs.get("verbose");
    boolean dryRun = parsedArgs.get("dry_run");
    boolean isJava = parsedArgs.get("is_java");

    if (parsedArgs.get("skip_dynamic_libraries")) {
      if (verbose) {
        System.out.println("Skipping dynamic libraries");
      }
      dynamicLibraryPaths = new ArrayList<>();
    }

    Deploy deploy = new Deploy(robotBinary, dynamicLibraryPaths, verbose, dryRun);
    deploy.deploy(parsedArgs.get("team_number"), isJava, runfiles);
  }
}
