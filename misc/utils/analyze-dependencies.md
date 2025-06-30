# Maven Dependencies Analysis

## Command for Dependency Tree Analysis

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dscope=compile
```

## Command for Brief View (first 20 lines)

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile | head -20"
```

## Alternative Options

### Full analysis of all scopes
```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree
```

### Analysis with detailed conflict information
```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dverbose=true
```

### Save results to file
```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile > dependency-tree.txt"
```

## Description

These commands use a Docker container with Maven and Java 21 to analyze project dependencies without requiring local Maven installation.

### What the commands do:

1. **`docker run --rm`** - runs a temporary Docker container that is automatically removed after execution
2. **`-v ${PWD}:/usr/src/app`** - mounts the current project directory into the container
3. **`-w /usr/src/app`** - sets the working directory inside the container
4. **`maven:3.9-eclipse-temurin-21`** - uses the official Maven image with Java 21 (Eclipse Temurin)
5. **`mvn dependency:tree`** - analyzes the dependency tree
6. **`-Dscope=compile`** - filters only compile dependencies
7. **`| head -20`** - shows only the first 20 lines of the result

### When to use:

- **After adding Vert.x BOM** - verify that versions are correctly managed
- When dependency conflicts arise
- To understand transitive dependencies
- When updating library versions
- For diagnosing ClassPath issues

### What to check in the output:

- ✅ All `io.vertx:*` modules have the same version (e.g., `3.9.13`)
- ✅ No lines with `(omitted for conflict)` for critical dependencies
- ✅ BOM successfully manages versions of Vert.x modules
- ✅ No duplicate dependencies with different versions

### Notes:

- Commands require Docker to be installed
- Execution happens in the context of the current project directory
- Make sure you're in the project root directory (where `pom.xml` is located)
- Uses Maven image with Java 21, matching the Java version in the project

### Example output:

```
[INFO] com.clickhouse:clickhouse-jdbc-bridge:jar:2.1.0-SNAPSHOT
[INFO] +- com.github.ben-manes.caffeine:caffeine:jar:3.2.1:compile
[INFO] +- com.zaxxer:HikariCP:jar:5.1.0:compile
[INFO] +- dnsjava:dnsjava:jar:3.6.3:compile
[INFO] +- io.vertx:vertx-core:jar:3.9.13:compile        ← Version from BOM
[INFO] +- io.vertx:vertx-config:jar:3.9.13:compile      ← Version from BOM
[INFO] +- io.vertx:vertx-web:jar:3.9.13:compile         ← Version from BOM
[INFO] |  +- io.vertx:vertx-bridge-common:jar:3.9.13:compile
[INFO] |  \- io.vertx:vertx-auth-common:jar:3.9.13:compile
[INFO] +- io.vertx:vertx-web-client:jar:3.9.13:compile  ← Version from BOM
[INFO] +- io.vertx:vertx-micrometer-metrics:jar:3.9.13:compile
[INFO] +- io.micrometer:micrometer-registry-prometheus:jar:1.1.19:compile
``` 