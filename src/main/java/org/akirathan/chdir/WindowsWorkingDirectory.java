package org.akirathan.chdir;

import java.util.List;
import org.graalvm.nativeimage.c.CContext;
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
      var ret = GetCurrentDirectoryA(4096, ptr);
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
    System.out.println("[WindowsWorkingDir] Changing working dir to " + path);
    try (var cPath = CTypeConversion.toCString(path + "\0")) {
      var ptr = cPath.get();
      var res = SetCurrentDirectoryA(ptr);
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
    System.out.println("[WindowsWorkingDir] exists: dir=" + dir + ", file=" + file);
    var path = dir + "\\" + file;
    try (var cPath = CTypeConversion.toCString(path)) {
      var ptr = cPath.get();
      var res = PathFileExistsA(ptr);
      System.out.println("[WindowsWorkingDir] exists: res = " + res);
      return res != 0;
    } catch (Throwable t) {
      System.err.println("Cannot check if " + path + " exists on Windows");
      throw t;
    }
  }

  @CFunction
  static native int GetCurrentDirectoryA(int nBufferLength, CCharPointer lpBuffer);

  @CFunction
  static native int SetCurrentDirectoryA(CCharPointer lpPathName);

  @CFunction
  static native int PathFileExistsA(CCharPointer pszPath);


  static final class Directives implements CContext.Directives {
    @Override
    public boolean isInConfiguration() {
      return Platform.getOperatingSystem().isWindows();
    }

    @Override
    public List<String> getHeaderFiles() {
      return List.of("<windows.h>", "<winbase.h>", "<shlwapi.h>");
    }

    @Override
    public List<String> getLibraries() {
      return List.of("Kernel32", "Shlwapi");
    }
  }
}
