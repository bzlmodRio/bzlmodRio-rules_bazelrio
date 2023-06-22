load("@rules_bazelrio//private:get_dynamic_deps.bzl", "get_dynamic_deps")
load("@rules_java//java:defs.bzl", "java_binary", "java_test")

def _symlink_java_native_libraries_impl(ctx):
    shared_lib_native_deps = []
    for dep in ctx.attr.deps:
        shared_lib_native_deps += get_dynamic_deps(dep)

    symlinks = []
    for lib in shared_lib_native_deps:
        out = ctx.actions.declare_file(ctx.attr.output_directory + "/" + lib.basename)
        ctx.actions.symlink(output = out, target_file = lib)
        symlinks.append(out)

    return [DefaultInfo(files = depset(symlinks), runfiles = ctx.runfiles(files = symlinks))]

_symlink_java_native_libraries = rule(
    attrs = {
        "deps": attr.label_list(mandatory = True),
        "output_directory": attr.string(mandatory = True),
    },
    implementation = _symlink_java_native_libraries_impl,
)

def bazelrio_java_binary(name, deps = [], data = [], runtime_deps = [], **kwargs):
    runfiles_suffix = _get_runfiles_suffix(name)

    # We must have the shared libraries live next to the binary
    native_shared_libraries_symlink = name + ".symlink_native"
    _symlink_java_native_libraries(
        name = native_shared_libraries_symlink,
        deps = deps + runtime_deps,
        output_directory = select({
            "@bazel_tools//src/conditions:windows": name + ".exe.runfiles",
            "//conditions:default": name + ".runfiles/" + runfiles_suffix,
        }),
    )

    java_binary(
        name = name,
        deps = deps,
        runtime_deps = runtime_deps,
        data = data + select({
            "//conditions:default": [native_shared_libraries_symlink],
        }),
        **kwargs
    )

def _get_runfiles_suffix(name):
    lbl = Label(native.repository_name() + "//" + native.package_name() + ":" + name)

    runfiles_suffix = "__main__"
    if str(lbl).startswith("@@"):
        runfiles_suffix = "_main"

    return runfiles_suffix

def bazelrio_java_test(name, deps = [], data = [], runtime_deps = [], **kwargs):
    runfiles_suffix = _get_runfiles_suffix(name)

    # We must have the shared libraries live next to the binary
    native_shared_libraries_symlink = name + ".symlink_native"
    _symlink_java_native_libraries(
        name = native_shared_libraries_symlink,
        deps = deps + runtime_deps,
        output_directory = select({
            "@bazel_tools//src/conditions:windows": name + ".exe.runfiles/" + runfiles_suffix,
            "//conditions:default": name + ".runfiles/" + runfiles_suffix,
        }),
        testonly = True,
    )

    java_test(
        name = name,
        deps = deps,
        runtime_deps = runtime_deps,
        data = data + select({
            "//conditions:default": [native_shared_libraries_symlink],
        }),
        **kwargs
    )
