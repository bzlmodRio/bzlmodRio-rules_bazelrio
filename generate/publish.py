import os
from bazelrio_gentool.publish_module import publish_module
from bazelrio_gentool.deps.dependency_container import DependencyContainer


def main():
    SCRIPT_DIR = os.environ["BUILD_WORKSPACE_DIRECTORY"]
    registry_location = os.path.join(
        SCRIPT_DIR, "..", "..", "..", "bazel-central-registry"
    )

    version = "0.0.11"
    year = "1"
    group = DependencyContainer(
        "rules_bazelrio", version, year, "https://frcmaven.wpi.edu/release"
    )

    module_template = os.path.join(SCRIPT_DIR, "module_config.jinja2")
    module_json_template = None

    os.chdir(SCRIPT_DIR)
    publish_module(registry_location, group, module_json_template, module_template)


if __name__ == "__main__":
    """
    bazel run //generate:publish_rules_bazelrio --enable_bzlmod
    """
    main()
