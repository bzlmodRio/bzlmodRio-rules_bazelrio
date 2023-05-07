java_library(
    name = "Deploy",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    # main_class = ""
    deps = [
        "@bazel_tools//tools/java/runfiles",
        "@rules_bazelrio_maven//:com_hierynomus_sshj",
        "@rules_bazelrio_maven//:me_tongfei_progressbar",
        "@rules_bazelrio_maven//:net_sourceforge_argparse4j_argparse4j",
        "@rules_bazelrio_maven//:org_slf4j_slf4j_nop",
    ],
)
