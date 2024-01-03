load("@rules_bazelrio//:halsim_defs.bzl", "halsim_cc_binary", "halsim_java_binary")
load("@rules_bazelrio//private:get_dynamic_deps.bzl", "get_dynamic_deps")
load("@rules_cc//cc:defs.bzl", "cc_binary")
load("@rules_java//java:defs.bzl", "java_binary")

def _get_dynamic_dependencies_impl(ctx):
    shared_lib_native_deps = get_dynamic_deps(ctx.attr.target)
    return [DefaultInfo(files = depset(shared_lib_native_deps))]

_get_dynamic_dependencies = rule(
    attrs = {
        "target": attr.label(
            mandatory = True,
        ),
    },
    implementation = _get_dynamic_dependencies_impl,
)

def _deploy_command(name, bin_name, lib_name, team_number, dry_run, verbose, is_java, visibility):
    discover_dynamic_deps_task_name = lib_name + ".discover_dynamic_deps"
    _get_dynamic_dependencies(
        name = discover_dynamic_deps_task_name,
        target = lib_name,
    )

    data = [bin_name, discover_dynamic_deps_task_name]

    args = [
        "--robot_binary",
        "$(location {})".format(bin_name),
        "--team_number",
        str(team_number),
        "--dynamic_libraries",
        "$(locations {})".format(discover_dynamic_deps_task_name),
    ]

    if dry_run:
        args.append("--dry_run")

    if verbose:
        args.append("--verbose")

    if is_java:
        args.append("--is_java")

    java_binary(
        name = name,
        runtime_deps = ["@rules_bazelrio//deploy/src/main/java/org/bazelrio/deploy"],
        main_class = "org.bazelrio.deploy.Main",
        visibility = visibility,
        args = args,
        data = data,
        target_compatible_with = [
            #  "@bazelrio//constraints/is_roborio:true",
        ],
    )

def robot_cc_binary(name, team_number, lib_name, halsim_deps = [], visibility = None, dry_run = False, verbose = False, **kwargs):
    deps = [":" + lib_name]

    cc_binary(
        name = name,
        deps = deps,
        visibility = visibility,
        **kwargs
    )

    if halsim_deps:
        halsim_cc_binary(
            name = name + ".sim",
            halsim_deps = halsim_deps,
            deps = deps,
            visibility = visibility,
        )

    _deploy_command(
        name = name + ".deploy",
        bin_name = name,
        lib_name = lib_name,
        team_number = team_number,
        visibility = visibility,
        dry_run = dry_run,
        verbose = verbose,
        is_java = False,
    )

def robot_java_binary(name, team_number, main_class, runtime_deps = [], halsim_deps = [], visibility = None, dry_run = False, verbose = False, **kwargs):
    java_binary(
        name = name,
        main_class = main_class,
        runtime_deps = runtime_deps,
        visibility = visibility,
        **kwargs
    )

    if halsim_deps:
        halsim_java_binary(
            visibility = visibility,
            name = name + ".sim",
            halsim_deps = halsim_deps,
            main_class = main_class,
            runtime_deps = runtime_deps,
            tags = ["no-sandbox"],
            jvm_flags = [
                "-Djava.library.path=.",
            ],
        )

    _deploy_command(
        name = name + ".deploy",
        bin_name = name + "_deploy.jar",
        lib_name = name,
        team_number = team_number,
        visibility = visibility,
        dry_run = dry_run,
        verbose = verbose,
        is_java = True,
    )
