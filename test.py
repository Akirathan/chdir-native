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
        maven_home = "C:\\Program Files\\Apache\\apache-maven-3.9.9"
        assert os.path.exists(maven_home), maven_home
        bin = os.path.join(maven_home, "bin", "mvn.cmd")
        assert os.path.exists(bin), bin
        return bin
    else:
        return "mvn"


if __name__ == '__main__':
    print(f"os.name = {os.name}")
    tmpdir = tempfile.mkdtemp(prefix="enso_test_proj")
    proj_dir = create_project(tmpdir, "Project")
    print(f"Project created at: {proj_dir}")

    cmd = [maven_binary(), "-P", "native", "clean", "compile", "native:compile-no-fork"]
    print(f"Running command: {cmd}")
    subprocess.run(cmd, check=True)
    target = os.path.join(os.getcwd(), "target", "chdir-native")
    assert os.path.exists(target), target
    my_vector = os.path.join(proj_dir, "src", "Data", "My_Vector.enso")
    assert os.path.exists(my_vector), my_vector
    ret = subprocess.run([target, "-cwd", my_vector],
                   capture_output=True,
                   text=True,
                   check=True)
    expected_ret = os.path.join(proj_dir, "MY_FILE.txt")
    last_returned_line = ret.stdout.splitlines()[-1]
    print(ret.stdout)
    print(ret.stderr)
    if last_returned_line != expected_ret:
        print("FAILURE")
        print(f"Expected: {expected_ret}")
        print(f"Actual: {last_returned_line}")
        exit(1)
    else:
        print("SUCCESS")
