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


if __name__ == '__main__':
    tmpdir = tempfile.mkdtemp(prefix="enso_test_proj")
    proj_dir = create_project(tmpdir, "Project")
    print(f"Project created at: {proj_dir}")
    subprocess.run(["mvn", "-P", "native", "clean", "compile", "native:compile-no-fork"], check=True)
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
