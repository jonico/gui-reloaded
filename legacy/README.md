# Eclipse PDE to Maven for the CCF Eclipse plugin

Baseline work only - no modernization. The PDE metadata (`MANIFEST.MF`, `build.properties`,
`feature.xml`) is left untouched; the Maven build sits alongside it.

Baseline JDK is **8**, matching the other CCF repositories.

## Why not Tycho

Tycho is the orthodox way to build OSGi bundles with Maven, but it resolves against a p2
target platform, and the target platform this code was written for (Eclipse 3.4/3.5, 2009 -
the manifests say `Bundle-RequiredExecutionEnvironment: J2SE-1.5`) is not reachable. Every
Eclipse bundle needed here is on Maven Central as an ordinary jar, so a plain Maven build
avoids inventing a target platform.

## One module, seven source roots

The seven bundles reference each other through `Require-Bundle` - six of the seven need
`com.collabnet.ccf`, and `com.collabnet.ccf.teamforge_sw` needs types from
`com.collabnet.ccf.teamforge`. Compiling them together resolves those references without OSGi
resolution. My first analysis wrongly concluded that
`com.collabnet.ccf.teamforge.schemageneration` was missing; it is present, supplied by a
sibling bundle, and only looked absent because the search had been over jars rather than
source.

## The Eclipse dependencies

Derived from the actual `import` statements rather than from `Require-Bundle`, which is both
more precise and caught bundles the manifests do not list. By usage: SWT (~800 imports),
JFace (~500), `org.eclipse.ui` and the forms/views/editors bundles, and
`org.eclipse.core.*`.

Two things make the Eclipse artifacts on Central awkward to consume as plain Maven
dependencies, and both had to be worked around:

1. They declare a dependency on `org.eclipse.swt.${osgi.platform}`. That placeholder is not
   interpolated from the consuming POM, and Maven rejects it as an invalid `artifactId`, so
   the build fails before compiling anything. `osgi.platform` is defined as a property *and*
   all Eclipse transitives are cut.
2. Some use version ranges - `[4.5.1,6.0.0)` for JNA, `[1.3.5,2.0.0)` for
   `javax.annotation-api` - that Maven cannot enumerate.

Every `org.eclipse.platform` dependency therefore carries a wildcard exclusion, and the
bundles actually needed are named explicitly. Note that `org.eclipse.swt` on its own contains
**no classes** (10 files of metadata); SWT is entirely platform-specific, so a fragment is
required - here `org.eclipse.swt.cocoa.macosx.aarch64`.

All chosen versions are bytecode **major 52 (Java 8)**, verified by reading the class files
rather than assuming from version numbers. That constraint is why the versions are ~2016-2021
rather than current: recent Eclipse releases target Java 17+.

## What does not compile: 13 of 267 files

Two package groups exist in no artifact anywhere and never appear in this repository's
history. They came from bundles published to a CCF update site that no longer exists.

| Missing | Types | Affected |
|---|---|---|
| `com.collabnet.ccf.api` / `.api.model` | 16 | the whole `com.collabnet.ccf.migration` bundle (9 files) |
| `com.collabnet.helm.ws` / `.domain` / `.project` | 8 | `PTClient` and its 3 transitive referrers in `com.collabnet.ccf.pt` |

`com.collabnet.ccf.migration` is dropped as a source root: nothing outside it references it.
For `.pt`, the referrer closure was computed rather than guessed - it is 4 files of 32
(`PTClient`, `ProjectTrackerSelectionDialog`, `ProjectTrackerMappingSection`,
`ProjectTrackerCcfParticipant`), so the other 28 still build.

Excluding files in `maven-compiler-plugin` is not by itself enough: javac compiles an excluded
file anyway if something else on the sourcepath references it, which is why the closure has to
be excluded rather than just the leaf.

Writing stubs for the 24 missing types was considered and rejected. Their signatures would be
guesswork, and a fabricated API that compiles is worse than an honestly absent one. This is
the opposite call from the `TeamForgeCompat` shim in `ccfmaster-reloaded`, where the missing
members' semantics were unambiguous from their use as a feature gate.

**254 of 267 sources compile, to 534 classes.**

## There are no tests

Zero JUnit usage anywhere in the repository. The seven files named `Test*.java` are
`main()` programs that generate schemas and XSLT, not tests.

This matters for judging any migration of this code: "the tests still pass" is not available
as a check here, and correctness rests on compilation alone.

## Reproducing

```bash
export JAVA_HOME=/path/to/jdk8
# ccf-core and the vendored SDKs come from the core repository first
MAVEN_REPO_LOCAL=~/some/repo ../core/legacy/bootstrap.sh
(cd ../core && mvn -Dmaven.repo.local=~/some/repo -DskipTests install)
MAVEN_REPO_LOCAL=~/some/repo ./legacy/bootstrap.sh
mvn -Dmaven.repo.local=~/some/repo compile
```
