package org.akirathan.chdir;

public enum Platform {
  LINUX,
  MACOS,
  WINDOWS;

  private static final String OS_NAME = "os.name";
  private static final String OS_LINUX = "linux";
  private static final String OS_MAC = "mac";
  private static final String OS_WINDOWS = "windows";

  private static final Platform OPERATING_SYSTEM = detectOperatingSystem();

  private static Platform detectOperatingSystem() {
    var osName = System.getProperty(OS_NAME);
    var lowerOsName = osName.toLowerCase();

    if (lowerOsName.contains(OS_LINUX)) {
      return LINUX;
    }

    if (lowerOsName.contains(OS_MAC)) {
      return MACOS;
    }

    if (lowerOsName.contains(OS_WINDOWS)) {
      return WINDOWS;
    }

    throw new IllegalStateException("Unknown Operrating System: '" + osName + "'");
  }

  public static Platform getOperatingSystem() {
    return OPERATING_SYSTEM;
  }

  public boolean isLinux() {
    return this == LINUX;
  }

  public boolean isMacOs() {
    return this == MACOS;
  }

  public boolean isWindows() {
    return this == WINDOWS;
  }
}
