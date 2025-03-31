package org.akirathan.chdir;

public interface WorkingDirectory {
  String currentWorkingDir();
  boolean changeWorkingDir(String path);
  boolean exists(String dir, String file);
}
