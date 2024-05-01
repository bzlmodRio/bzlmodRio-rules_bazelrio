package org.bazelrio.jnidebugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("PMD")
public final class JniDebugger {
  private JniDebugger() {}

  public static void printStandardDebugInfoUnsafe() throws IOException {
    System.out.println("-------------------------------------------\n");
    System.out.println("*******************************************");
    System.out.println("LD_LIBRARY_PATH");
    System.out.println(System.getenv("LD_LIBRARY_PATH"));
    System.out.println("*******************************************");
    System.out.println("DYLD_LIBRARY_PATH");
    System.out.println(System.getenv("DYLD_LIBRARY_PATH"));
    System.out.println("*******************************************");
    System.out.println("PATH");
    System.out.println(System.getenv("PATH"));
    System.out.println("*******************************************");
    System.out.println("User Dir");
    System.out.println(System.getProperty("user.dir"));
    System.out.println("*******************************************");
    System.out.println("Java Library Path");
    System.out.println(System.getProperty("java.library.path"));
    System.out.println("*******************************************");

    System.out.println("Files:");
    try (DirectoryStream<Path> stream =
        Files.newDirectoryStream(Paths.get(System.getProperty("user.dir")))) {
      for (Path path : stream) {
        if (!Files.isDirectory(path)) {
          System.out.println("  " + path);
        }
      }
    }
    System.out.println("-------------------------------------------\n");
  }

  public static void printStandardDebugInfo() {
    try {
      printStandardDebugInfoUnsafe();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void printSharedLibDependents(String sharedObjectName) {
    printSharedLibDependents(sharedObjectName, "");
  }
    
  public static void printSharedLibDependents(String sharedObjectName, String suffix) {
    System.out.println("-------------------------------------------\n");
    System.out.println("Dumping LDD for " + sharedObjectName);

    try {
      String command = "dummy";

      String osName = System.getProperty("os.name");
      if (osName.contains("Linux")) {
        command = "ldd lib" + sharedObjectName + ".so" + suffix;
      } else if (osName.contains("Mac")) {
        command = "otool -L lib" + sharedObjectName + suffix + ".dylib";
      } else if (osName.contains("Windows")) {
        System.err.println("Not easy to query on windows");
      } else {
        System.err.println("Unknown os '" + osName + "'");
      }

      System.out.println("  Running '" + command + "'");

      Runtime r = Runtime.getRuntime();
      Process p = r.exec(command);
      p.waitFor();
      BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";

      while ((line = b.readLine()) != null) {
        System.out.println(line);
      }
      
      System.out.println("-------------------------------------------\n");

      b.close();
    } catch (Exception ex) {
      System.err.println("Could not load ldd data");
    }
  }

  public static void attemptToLoadLibrary(String libraryName) {
    System.out.println("-------------------------------------------\n");
    System.out.println("Attempting to load library: '" + libraryName + "'");
    try {
      System.loadLibrary(libraryName);
    } catch (UnsatisfiedLinkError ex) {
      ex.printStackTrace();
    }
    System.out.println("-------------------------------------------\n");
  }
}
