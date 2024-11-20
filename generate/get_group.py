from bazelrio_gentool.deps.dependency_container import DependencyContainer
from get_version import VERSION


def get_rules_bazelrio_group():
    version = VERSION
    year = "1"
    group = DependencyContainer("rules_bazelrio", version, year, "")

    return group
