# ClickHouse JDBC Bridge Project Utilities

This folder contains useful utilities for working with the project through Docker without the need for local Maven installation.

## 📋 Available Utilities

### 1. 🔍 Maven Dependencies Analysis

**File:** `analyze-dependencies.md` - detailed documentation with Docker commands

**Purpose:** Analyzing the project dependency tree to diagnose conflicts and verify Vert.x BOM functionality.

### 2. 📄 NOTICE File Update

**File:** `update-notice.md`

**Purpose:** Automatic update of the NOTICE file based on current project dependencies.

## 🚀 Quick Start

### Check Vert.x BOM functionality (brief output)

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile | head -20"
```

### Full dependency analysis

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dscope=compile
```

### Detailed analysis with conflicts

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dverbose=true
```

### Save results to file

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile > dependency-tree.txt"
```

## 🔧 Requirements

- **Docker** - for executing Maven commands
- Be in the project root directory (where `pom.xml` is located)

## 📝 Usage Examples

### After adding Vert.x BOM

1. **Check BOM functionality (brief output):**
   ```bash
   docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile | head -20"
   ```

2. **Find all Vert.x dependencies:**
   ```bash
   docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dscope=compile | grep "io.vertx"
   ```

3. **Find dependency conflicts:**
   ```bash
   docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dverbose=true | grep -E "(conflict|omitted)"
   ```

## 🎯 What to Check in Output

### ✅ Correct BOM functionality:
- All `io.vertx:*` modules have the **same version** `3.9.13`
- No lines with `(omitted for conflict)` for Vert.x modules

### ❌ BOM issues:
- Different versions of Vert.x modules
- Conflicts between `io.vertx:*` dependencies
- Unexpected transitive dependencies

### Expected result:
```
[INFO] +- io.vertx:vertx-core:jar:3.9.13:compile        ← Version from BOM
[INFO] +- io.vertx:vertx-config:jar:3.9.13:compile      ← Version from BOM
[INFO] +- io.vertx:vertx-web:jar:3.9.13:compile         ← Version from BOM
[INFO] +- io.vertx:vertx-web-client:jar:3.9.13:compile  ← Version from BOM
[INFO] +- io.vertx:vertx-micrometer-metrics:jar:3.9.13:compile ← Version from BOM
```

## 🔄 Workflow for Changes

1. **Changed pom.xml** → check dependencies
2. **Updated versions** → run detailed analysis
3. **Before commit** → update NOTICE file
4. **When problems occur** → save results to file for detailed analysis

## 📞 Support

If commands don't work:

1. **Check Docker:** `docker --version`
2. **Check location:** make sure you're in the project root
3. **Windows PowerShell:** use PowerShell instead of cmd for better Docker compatibility

All commands use the official Maven image with Java 21, which corresponds to the Java version in the project. 