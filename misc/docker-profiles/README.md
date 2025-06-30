# Docker Build Profiles for ClickHouse JDBC Bridge

This directory contains Docker build configuration for the ClickHouse JDBC Bridge with Java 21 LTS.

## 📁 Available Dockerfiles

### 🚀 `Dockerfile.standard` - Standard JAR Build
**Purpose**: Creates a traditional JVM-based Docker image using regular JAR files.

**Build Profile**: Default Maven profile (standard compilation)
**Runtime**: Eclipse Temurin 21 JRE
**Output**: Shaded JAR file executed by JVM

## 🔧 Build Commands

### Standard Build
```bash
# Build standard Docker image
docker build -f misc/docker-profiles/Dockerfile.standard -t clickhouse-jdbc-bridge:standard .

# Run standard container
docker run -d -p 9019:9019 --name jdbc-bridge-standard clickhouse-jdbc-bridge:standard

# Run with JMX profiling enabled
docker run -d -p 9019:9019 -p 9999:9999 --name jdbc-bridge-standard-profiling -e JDBC_BRIDGE_JVM_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" clickhouse-jdbc-bridge:standard

# Debug: Run container with interactive bash for troubleshooting
docker run -it --entrypoint /bin/bash clickhouse-jdbc-bridge:standard
```

## 📊 Build Details

| Aspect | Standard Build |
|--------|----------------|
| **Build Time** | ~2-3 minutes |
| **Image Size** | ~400-500 MB |
| **Startup Time** | ~3-5 seconds |
| **Memory Usage** | 150-300 MB |
| **Runtime** | JVM (HotSpot) |
| **Base Image** | eclipse-temurin:21-jre |
| **JIT Compilation** | Yes (runtime optimization) |
| **Debugging** | Full JVM debugging |

## 🎯 When to Use

### 🚀 Standard Build - Use When:
- **Development and testing** environments
- Need **full JVM debugging** capabilities
- Using **JVM-specific tools** (JProfiler, VisualVM, etc.)
- **Runtime flexibility** is important
- **Compatibility** with all Java libraries is required
- **JIT optimization** benefits long-running processes

## 🏗️ Technical Details

### Standard Build Architecture
```
┌─────────────────────────────────────┐
│ Stage 1: Builder                    │
│ ├─ maven:3.9-eclipse-temurin-21     │
│ ├─ mvn clean package                │
│ └─ Output: JAR files                │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ Stage 2: Runtime                    │
│ ├─ eclipse-temurin:21-jre           │
│ ├─ Copy JAR files                   │
│ └─ Run: java -jar app.jar           │
└─────────────────────────────────────┘
```

## 🔍 Verification

### Health Checks
The image includes health checks:
```bash
# Check container health
docker ps

# Manual health check
curl http://localhost:9019/ping
```

### Performance Testing
```bash
# Measure startup time
time docker run --rm clickhouse-jdbc-bridge:standard echo "Started"

# Memory usage monitoring
docker stats jdbc-bridge-standard
```

## 🐛 Troubleshooting

### Standard Build Issues
- **Out of Memory**: Increase Docker memory allocation
- **Build Timeout**: Check network connectivity for dependencies
- **JVM Crashes**: Review JVM options in docker-entrypoint.sh

## 📈 Monitoring and Profiling

### Standard Build Monitoring
- Use **VisualVM**, **JProfiler**, or similar JVM tools
- **JMX** endpoints available for monitoring
- **JFR** (Java Flight Recorder) for performance analysis

#### JMX Profiling with VisualVM
```bash
# Run container with JMX enabled
docker run -d -p 9019:9019 -p 9999:9999 --name jdbc-bridge-standard-profiling \
  -e JDBC_BRIDGE_JVM_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
  clickhouse-jdbc-bridge:standard

# Connect VisualVM to localhost:9999
# 1. Right-click "Remote" in VisualVM
# 2. Add JMX Connection: localhost:9999
# 3. Uncheck "Use security credentials"
# 4. Connect and monitor
```

#### JVM Options Explained:
- `-Dcom.sun.management.jmxremote`: Enable JMX remote monitoring
- `-Dcom.sun.management.jmxremote.port=9999`: JMX port
- `-Dcom.sun.management.jmxremote.rmi.port=9999`: RMI port (same as JMX)
- `-Dcom.sun.management.jmxremote.authenticate=false`: Disable authentication
- `-Dcom.sun.management.jmxremote.ssl=false`: Disable SSL
- `-Djava.rmi.server.hostname=localhost`: Set RMI hostname

#### Standard Build Performance Monitoring
```bash
# Monitor container resources
docker stats jdbc-bridge-standard

# Application logs
docker logs -f jdbc-bridge-standard

# System-level monitoring
docker exec jdbc-bridge-standard top
docker exec jdbc-bridge-standard ps aux
```

## 🔄 CI/CD Integration

### Example GitHub Actions
```yaml
# Standard build
- name: Build Standard Image
  run: docker build -f misc/docker-profiles/Dockerfile.standard -t app:standard .
```

### Example Jenkins Pipeline
```groovy
stage('Build Images') {
    steps {
        sh 'docker build -f misc/docker-profiles/Dockerfile.standard -t app:standard .'
    }
}
```

## 📝 Notes

- **Standard builds** are more compatible and easier to debug
- The image uses **Java 21 LTS** for long-term support
- **Multi-stage builds** minimize final image size
- **Health checks** ensure container readiness
- **Volume mounts** allow external configuration and drivers 