package org.akirathan.chdir;

import java.io.File;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
  private static final Option CWD =
      new Option("cwd", true, "Path to change directory to");
  private static final Option PWD =
      new Option("pwd", false, "Print current working directory");
  private static final Option CHROOT =
      new Option("chroot", true, """
          Given file in Enso project, detects the project root, and changes the directory
          to that root.
          """);

  private static final Options OPTIONS =
      new Options()
          .addOption(CWD)
          .addOption(PWD)
          .addOption(CHROOT);

  public static void main(String[] args) throws ParseException {
    var parser = new DefaultParser();
    var line = parser.parse(OPTIONS, args);
    if (!line.hasOption(CWD) && !line.hasOption(PWD) && !line.hasOption(CHROOT)) {
      System.err.println("Usage: provide PWD, CWD, or CHROOT");
      System.exit(1);
    }
    var nativeApi = getWorkingDirForCurrentPlatform();
    if (line.hasOption(PWD)) {
      var curWDir = nativeApi.currentWorkingDir();
      System.out.println(curWDir);
    } else if (line.hasOption(CWD)) {
      var dir = line.getOptionValue(CWD);
      System.out.println("Changing directory to " + dir);
      var succ = nativeApi.changeWorkingDir(dir);
      if (!succ) {
        System.err.println("Changing directory to " + dir + " FAILED");
        System.exit(1);
      }
      var curDir = nativeApi.currentWorkingDir();
      System.out.println(curDir);
    } else if (line.hasOption(CHROOT)) {
      var ensoFile = line.getOptionValue(CHROOT);
      System.out.println("Detecting project root of " + ensoFile);
      var projectRoot = ensoFile;
      while (projectRoot != null) {
        if (nativeApi.exists(projectRoot, "package.yaml")) {
          nativeApi.changeWorkingDir(projectRoot);
          break;
        }
        var lastSlash = projectRoot.lastIndexOf('/');
        if (lastSlash == -1) {
          projectRoot = null;
        } else {
          projectRoot = projectRoot.substring(0, lastSlash);
        }
      }
      printCurWorkingDir();
    }
  }

  private static void printCurWorkingDir() {
    var linuxApi = new LinuxWorkingDirectory();
    var userDir = System.getProperty("user.dir");
    System.out.println("user.dir system prop = " + userDir);
    var pwd = linuxApi.invokePwd();
    System.out.println("pwd = " + pwd);
    var cwd = linuxApi.currentWorkingDir();
    System.out.println("getcwd = " + cwd);

    var f = new File("MY_FILE.txt");
    System.out.println("new File(\"MY_FILE.txt\").getAbsolutePath() = " + f.getAbsolutePath());
    System.out.println(f.getAbsolutePath());
  }

  private static WorkingDirectory getWorkingDirForCurrentPlatform() {
    return switch (Platform.getOperatingSystem()) {
      case LINUX -> new LinuxWorkingDirectory();
      case WINDOWS -> new WindowsWorkingDirectory();
      case MACOS -> throw new IllegalStateException("Mac not supported yet");
    };
  }
}
