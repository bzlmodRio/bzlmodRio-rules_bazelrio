
import os
import argparse
from bazelrio_gentool.cli import add_generic_cli, GenericCliArgs
from bazelrio_gentool.generate_shared_files import (
    write_shared_root_files,
)


def main():
    SCRIPT_DIR = os.environ["BUILD_WORKSPACE_DIRECTORY"]
    REPO_DIR = os.path.join(SCRIPT_DIR, "..")

    parser = argparse.ArgumentParser()
    add_generic_cli(parser)
    args = parser.parse_args()

    # write_shared_root_files(REPO_DIR, None)
    

if __name__ == "__main__":
    main()
