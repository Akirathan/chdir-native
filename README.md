How does `java.io` react if we change directory via `chdir` syscall in NativeImage?

Build native image with `mvn -P native clean compile native:compile-no-fork` and run `./target/chdir-native`:
```
user.dir system prop = /home/pavel/dev/chdir-native
pwd = /home/pavel/dev/chdir-native
getcwd = /home/pavel/dev/chdir-native
new File("MY_FILE.txt").getAbsolutePath() = /home/pavel/dev/chdir-native/MY_FILE.txt
=== Changing working directory to /tmp ===
user.dir system prop = /tmp
pwd = /tmp
getcwd = /tmp
new File("MY_FILE.txt").getAbsolutePath() = /home/pavel/dev/chdir-native/MY_FILE.txt
```

The processe's cwd was successfully changed, but `java.io` still thinks the current
directory is the one where the process was started.

## GraalVM JDK
```
$ java -version
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30, mixed mode, sharing)
```
