package org.akirathan.chdir;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

@CContext(LinuxWorkingDirectory.Directives.class)
public final class LinuxWorkingDirectory {
  private static final String PWD = "pwd";

  public boolean changeWorkingDir(String path) {
    try (var cPath = CTypeConversion.toCString(path + "\0")) {
      int res = chdir(cPath.get());
      if (res != 0) {
        System.err.println("chdir(" + path + ") syscall returned " + res);
        return false;
      }
      return true;
    } catch (Throwable e) {
      if (!ImageInfo.inImageRuntimeCode()) {
        System.err.println("Changing working directory is not supported in non-AOT mode");
        e.printStackTrace(System.err);
      } else {
        System.err.println("Cannot change working directory to " + path + " on Linux");
        e.printStackTrace(System.err);
      }
      return false;
    }
  }

  public String currentWorkingDir() {
    String cwd;
    try {
      cwd = invokeCwd();
    } catch (Throwable t) {
      if (!ImageInfo.inImageRuntimeCode()) {
        System.err.println("getcwd syscall is not supported in non-AOT mode");
      } else {
        System.err.println("Cannot invoke `getcwd` on Linux");
      }
      throw t;
    }
    return cwd;
  }

  @CFunction
  static native int chdir(CCharPointer path);
  @CFunction
  static native CCharPointer getcwd(CCharPointer buf, int size);

  String invokePwd() {
    try {
      var process = new ProcessBuilder(PWD).start();
      process.waitFor(3, TimeUnit.SECONDS);
      var pwd =
          new String(
              process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      return pwd.trim();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String invokeCwd() {
    byte[] buf = new byte[4096];
    String path;
    try (var ptrHolder = CTypeConversion.toCBytes(buf)) {
      var ptr = ptrHolder.get();
      var retPtr = getcwd(ptr, 4096);
      if (retPtr.isNull()) {
        System.err.println("getcwd() syscall returned null");
      }
      if (!retPtr.equal(ptr)) {
        System.err.println("getcwd() syscall returned different pointer");
      }
      path = new String(buf);
    }
    return path.trim();
  }

  static final class Directives implements CContext.Directives {

    @Override
    public boolean isInConfiguration() {
      return true;
    }

    @Override
    public List<String> getHeaderFiles() {
      return List.of("<unistd.h>");
    }

    @Override
    public List<String> getLibraries() {
      return List.of("c");
    }
  }
}