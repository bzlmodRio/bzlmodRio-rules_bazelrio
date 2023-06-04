load("@rules_bazelrio//:java_rules.bzl", "bazelrio_java_binary")
load("@rules_cc//cc:defs.bzl", "cc_binary")

def __prepare_halsim(halsim_deps):
    extension_names = []
    for dep in halsim_deps:
        lbl = Label(dep)
        name = lbl.name
        extension_names.append(name)

    return extension_names

def halsim_cc_binary(
        name,
        deps = [],
        halsim_deps = [],
        **kwargs):
    extension_names = __prepare_halsim(halsim_deps)
    env = select({
        "@bazel_tools//src/conditions:windows": {"HALSIM_EXTENSIONS": ";".join(extension_names)},
        "//conditions:default": {"HALSIM_EXTENSIONS": ":".join(extension_names)},
    })

    cc_binary(
        name = name,
        deps = deps + halsim_deps,
        env = env,
        **kwargs
    )

def halsim_java_binary(
        name,
        runtime_deps = [],
        halsim_deps = [],
        **kwargs):
    extension_names = __prepare_halsim(halsim_deps)
    env = select({
        "@bazel_tools//src/conditions:windows": {"HALSIM_EXTENSIONS": ";".join(extension_names)},
        "//conditions:default": {"HALSIM_EXTENSIONS": ":".join(extension_names), "LD_LIBRARY_PATH": "."},
    })

    bazelrio_java_binary(
        name = name,
        runtime_deps = runtime_deps + halsim_deps,
        env = env,
        **kwargs
    )
