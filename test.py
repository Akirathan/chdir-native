import os
import tempfile
import subprocess

def create_project(path: str, name: str) -> str:
    """ Returns absolute path to the created project directory """
    assert os.path.exists(path) and os.path.isdir(path), path
    assert os.path.isabs(path), path
    proj_dir = os.path.join(path, name)
    os.mkdir(proj_dir)
    with open(os.path.join(proj_dir, "package.yaml"), "w") as f:
        f.write("""
        name: My_Lib
        version: 0.0.0-dev
        """)
    os.mkdir(os.path.join(proj_dir, "src"))
    with open(os.path.join(proj_dir, "src", "Main.enso"), "w") as f:
        f.write("""
        import project.data.My_Vector.My_Vector
        
        main = My_Vector
        """)
    os.mkdir(os.path.join(proj_dir, "src", "Data"))
    with open(os.path.join(proj_dir, "src", "Data", "My_Vector.enso"), "w") as f:
        f.write("""
        type My_Vector
        """)
    return proj_dir


def maven_binary() -> str:
    if "win" in os.name.lower() or "nt" in os.name:
        print("Looking for Maven binary on Windows")
        maven_home = "C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-3.9.9"
        assert os.path.exists(maven_home), maven_home
        bin = os.path.join(maven_home, "bin", "mvn.cmd")
        assert os.path.exists(bin), bin
        return bin
    else:
        return "mvn"


def test_pwd(target: str):
    tmpdir = tempfile.mkdtemp(prefix="test_pwd")
    ret = subprocess.run([target, "-pwd"],
                         capture_output=True,
                         text=True,
                         check=True,
                         cwd=tmpdir)
    print(ret.stdout)
    print(ret.stderr)
    last_line = ret.stdout.splitlines()[-1]
    if last_line != tmpdir:
        print("FAILURE in test_pwd")
        print(f"Expected {tmpdir}, got {last_line}")
        exit(1)
    else:
        print("SUCCESS in test_pwd")


def test_cwd(target: str):
    tmpdir = tempfile.mkdtemp(prefix="test_cwd")
    cmd = [target, "-cwd", tmpdir]
    print(f"Running {cmd}")
    ret = subprocess.run(cmd,
                          capture_output=True,
                          text=True,
                          check=True)
    print(ret.stdout)
    print(ret.stderr)
    last_line = ret.stdout.splitlines()[-1]
    if last_line != tmpdir:
        print("FAILURE in test_cwd")
        print(f"Expected {tmpdir}, got {last_line}")
        exit(1)
    else:
        print("SUCCESS in test_cwd")


def test_proj_root_chdir(target: str):
    tmpdir = tempfile.mkdtemp(prefix="enso_test_proj")
    proj_dir = create_project(tmpdir, "Project")
    print(f"Project created at: {proj_dir}")
    my_vector = os.path.join(proj_dir, "src", "Data", "My_Vector.enso")
    assert os.path.exists(my_vector), my_vector
    cmd = [target, "-chroot", my_vector]
    print(f"Running {cmd}")
    ret = subprocess.run(cmd,
                         capture_output=True,
                         text=True,
                         check=True)
    expected_ret = os.path.join(proj_dir, "MY_FILE.txt")
    last_returned_line = ret.stdout.splitlines()[-1]
    print(ret.stdout)
    print(ret.stderr)
    if last_returned_line != expected_ret:
        print("FAILURE in test_proj_root_chdir")
        print(f"Expected: {expected_ret}")
        print(f"Actual: {last_returned_line}")
        exit(1)
    else:
        print("SUCCESS in test_proj_root_chdir")



if __name__ == '__main__':
    print(f"os.name = {os.name}")
    cmd = [maven_binary(), "-P", "native", "clean", "compile", "native:compile-no-fork"]
    print(f"Running command: {cmd}")
    subprocess.run(cmd, check=True)
    target = os.path.join(os.getcwd(), "target", "chdir-native")
    assert os.path.exists(target), target

    print("=== test_pwd ===")
    test_pwd(target)
    print("=== test_cwd ===")
    test_cwd(target)
    print("=== test_proj_root_chdir ===")
    test_proj_root_chdir(target)
