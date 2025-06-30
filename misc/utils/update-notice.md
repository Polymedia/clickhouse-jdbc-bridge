# Updating NOTICE File

## Command for Automatic NOTICE File Update

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn org.jasig.maven:maven-notice-plugin:generate
```

## Description

This command uses a Docker container with Maven and Java 21 to automatically generate the NOTICE file based on current project dependencies.

### What the command does:

1. **`docker run --rm`** - runs a temporary Docker container that is automatically removed after execution
2. **`-v ${PWD}:/usr/src/app`** - mounts the current project directory into the container
3. **`-w /usr/src/app`** - sets the working directory inside the container
4. **`maven:3.9-eclipse-temurin-21`** - uses the official Maven image with Java 21 (Eclipse Temurin)
5. **`mvn org.jasig.maven:maven-notice-plugin:generate`** - runs the Maven plugin to generate the NOTICE file

### When to use:

- After changing dependencies in `pom.xml`
- When updating library versions
- When Maven build fails with NOTICE file mismatch error
- Before release to ensure license information is up to date

### Notes:

- Command requires Docker to be installed
- The NOTICE file will be overwritten if it already exists
- Make sure to use the correct Maven image with Java 21, matching the Java version in the project 