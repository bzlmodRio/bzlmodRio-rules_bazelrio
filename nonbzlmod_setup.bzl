load("@rules_jvm_external//:defs.bzl", "maven_install")

def setup_rules_bazelrio():
    maven_install(
        name = "rules_bazelrio_maven",
        artifacts = [
            "com.hierynomus:sshj:0.32.0",
            "me.tongfei:progressbar:0.9.2",
            "net.sourceforge.argparse4j:argparse4j:0.9.0",
            "org.slf4j:slf4j-nop:1.7.32",
        ],
        repositories = [
            "https://repo1.maven.org/maven2",
        ],
    )
