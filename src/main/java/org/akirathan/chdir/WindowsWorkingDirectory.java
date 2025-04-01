package org.akirathan.chdir;

import java.util.List;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.CContext.Directives;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

@CContext(WindowsWorkingDirectory.Directives.class)
public final class WindowsWorkingDirectory implements WorkingDirectory {

  @Override
  public String currentWorkingDir() {
    byte[] buf = new byte[4096];
    String path;
    try (var ptrHolder = CTypeConversion.toCBytes(buf)) {
      var ptr = ptrHolder.get();
      var ret = GetCurrentDirectory(4096, ptr);
      if (ret == 0) {
        System.err.println("GetCurrentDirectory failed");
        return null;
      } else {
        path = new String(buf);
      }
    }
    return path.trim();
  }

  @Override
  public boolean changeWorkingDir(String path) {
    try (var cPath = CTypeConversion.toCString(path + "\0")) {
      var res = SetCurrentDirectory(cPath.get());
      if (res == 0) {
        System.err.println("SetCurrentDirectory failed");
        return false;
      }
      return true;
    } catch (Throwable t) {
      System.err.println("Cannot change working directory to "
        + path
        + " on Windows");
      throw t;
    }
  }

  @Override
  public boolean exists(String dir, String file) {
    throw new UnsupportedOperationException("unimplemented");
  }

  @CFunction
  static native int GetCurrentDirectory(int nBufferLength, CCharPointer lpBuffer);

  @CFunction
  static native int SetCurrentDirectory(CCharPointer lpPathName);


  static final class Directives implements CContext.Directives {
    @Override
    public boolean isInConfiguration() {
      return Platform.getOperatingSystem().isWindows();
    }

    @Override
    public List<String> getHeaderFiles() {
      return List.of("<windows.h>", "<winbase.h>");
    }

    @Override
    public List<String> getLibraries() {
      return List.of("shell32", "Kernel32");
    }
  }
}
