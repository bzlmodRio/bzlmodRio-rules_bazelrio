import os
from bazelrio_gentool.publish_module import publish_module
from get_group import get_rules_bazelrio_group



def main():
    SCRIPT_DIR = os.environ["BUILD_WORKSPACE_DIRECTORY"]
    registry_location = os.path.join(
        SCRIPT_DIR, "..", "..", "..", "bazel-central-registry"
    )

    group = get_rules_bazelrio_group()

    module_template = os.path.join(SCRIPT_DIR, "templates/MODULE.bazel.jinja2")
    module_json_template = None

    os.chdir(SCRIPT_DIR)
    publish_module(registry_location, group, module_json_template, module_template)


if __name__ == "__main__":
    """
    bazel run //generate:publish_rules_bazelrio --enable_bzlmod
    """
    main()
