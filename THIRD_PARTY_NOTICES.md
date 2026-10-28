# Third-party notices

The packaged application has no third-party runtime dependencies. The Java standard library is supplied by the end user's JDK during development and by the bundled JDK runtime in the portable distribution.

Build/test tooling:

- JUnit Jupiter 5.11.4 and JUnit Platform 1.11.4 (including their API, params, engine, and commons modules) are test-scoped and distributed under the Eclipse Public License 2.0.
- OpenTest4J 1.3.0 and API Guardian 1.1.2 are transitive test dependencies distributed under the Apache License 2.0.
- These test dependencies are not included in the packaged application.
- Maven compiler, Surefire, and JAR plugins are build-time tools and are not included in the application image.
- GitHub Actions workflow uses `actions/checkout`, `actions/setup-java`, `actions/upload-artifact`, and `softprops/action-gh-release`; these are workflow-time actions, not part of the application bundle.

The application icon is an original generated project asset. No third-party image, map, or research dataset is bundled. External sources are cited in the bundled record and [DATA_SOURCES.md](DATA_SOURCES.md). The JDK is redistributed by `jpackage`; refer to the JDK distribution's license notices in the generated runtime directory.
