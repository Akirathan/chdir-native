Emulate the behavior of [Enso cmdline launcher](https://github.com/enso-org/enso/blob/2f71da28a4d2a67dac4998484d7a9449c58c5210/engine/runner/src/main/java/org/enso/runner/Utils.java#L64).
This repository is minimal testing for https://github.com/enso-org/enso/pull/12618
Build native image with
```
mvn -P native clean compile native:compile-no-fork
```
Run native image with
```
./target/chdir-native -pwd ~/dev/enso/test/Base_Tests/src/Data/Vector_Spec.enso
```
It should detect the project root and print it as the last line in stdout:
```
user.dir system prop = /home/pavel/dev/enso/test/Base_Tests
pwd = /home/pavel/dev/enso/test/Base_Tests
getcwd = /home/pavel/dev/enso/test/Base_Tests
new File("MY_FILE.txt").getAbsolutePath() = /home/pavel/dev/enso/test/Base_Tests/MY_FILE.txt
/home/pavel/dev/enso/test/Base_Tests/MY_FILE.txt
```


## GraalVM JDK
```
$ java -version
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30, mixed mode, sharing)
```
