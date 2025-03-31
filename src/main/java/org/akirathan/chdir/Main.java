package org.akirathan.chdir;

import java.io.File;

public class Main {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: provide argument with path to CWD to");
    } else {
      var linuxApi = new LinuxWorkingDirectory();
      linuxApi.changeWorkingDir(args[0]);
    }
    printCurWorkingDir();
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
  }
}
