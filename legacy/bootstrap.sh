#!/bin/bash
# Install the two jars gui vendors that are not on Maven Central.
# tfapi and cee-5.0 come from the core repository's bootstrap; run that one first.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${JAVA_HOME:?set JAVA_HOME to a JDK 8}"
REPO_ARG=""
[ -n "${MAVEN_REPO_LOCAL:-}" ] && REPO_ARG="-Dmaven.repo.local=$MAVEN_REPO_LOCAL"

echo "==> jacob"
mvn -B -q $REPO_ARG install:install-file -Dfile="com.collabnet.ccf.qc/lib/jacob.jar" \
    -DgroupId=ccf.vendored -DartifactId=jacob -Dversion=0 -Dpackaging=jar
echo "==> scrumworks-soap"
mvn -B -q $REPO_ARG install:install-file -Dfile="com.collabnet.ccf.sw/lib/ScrumWorks-soap.jar" \
    -DgroupId=ccf.vendored -DartifactId=scrumworks-soap -Dversion=0 -Dpackaging=jar
echo "==> done"
