# Running ClickHouse JDBC Bridge in Container and Profiling

This guide describes how to run ClickHouse JDBC Bridge in Docker container with Java 21 LTS and connect to it using VisualVM for profiling and monitoring.

## Prerequisites

- Docker installed and running
- VisualVM 2.0+ installed (tested with VisualVM 2.2)
- Built Docker image: `clickhouse-jdbc-bridge:standard`

## Building the Docker Image

First, build the standard Docker image with Java 21:

```shell
docker build -f misc/docker-profiles/Dockerfile.standard -t clickhouse-jdbc-bridge:standard .
```

## Running Container with JMX Enabled

Run the container with JMX monitoring enabled:

```shell
docker run -d -p 9019:9019 -p 9999:9999 --name jdbc-bridge-standard-profiling -e JDBC_BRIDGE_JVM_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" clickhouse-jdbc-bridge:standard
```

### Port Mapping:
- **9019**: HTTP port for JDBC Bridge API
- **9999**: JMX port for remote monitoring

### JVM Options Explained:
- `-Dcom.sun.management.jmxremote`: Enable JMX remote monitoring
- `-Dcom.sun.management.jmxremote.port=9999`: JMX port
- `-Dcom.sun.management.jmxremote.rmi.port=9999`: RMI port (same as JMX for simplicity)
- `-Dcom.sun.management.jmxremote.authenticate=false`: Disable authentication
- `-Dcom.sun.management.jmxremote.ssl=false`: Disable SSL
- `-Djava.rmi.server.hostname=localhost`: Set RMI hostname

## Connecting with VisualVM

### Step 1: Launch VisualVM
Start VisualVM 2.2 or later version.

### Step 2: Add JMX Connection
1. In the left panel, right-click on **"Remote"**
2. Select **"Add JMX Connection..."**

### Step 3: Configure Connection
- **Connection**: `localhost:9999`
- **Display name**: `ClickHouse JDBC Bridge (Java 21)`
- **Security**: Uncheck "Use security credentials"
- Click **"OK"**

### Step 4: Connect
1. Find the new connection in the tree
2. Double-click to connect
3. VisualVM will show the JVM monitoring interface

## Available Monitoring Features

Once connected, you can monitor:

- **Overview**: JVM version, system properties, JVM arguments
- **Monitor**: Real-time CPU usage, memory consumption, loaded classes, threads
- **Threads**: Thread states and stack traces
- **Sampler**: CPU and memory profiling
- **MBeans**: JMX management beans for detailed monitoring

## Verification

### Check Application Status
```shell
curl http://localhost:9019/ping
```
Should return: `Ok.` with HTTP 200 status

### Check JMX Port
```shell
telnet localhost 9999
```
Should connect successfully without errors

### Check Container Logs
```shell
docker logs jdbc-bridge-standard-profiling
```
Should show successful startup without JMX-related errors

## Troubleshooting

### Connection Issues
1. Verify ports are open: `netstat -an | findstr 9999`
2. Check container is running: `docker ps`
3. Review container logs: `docker logs jdbc-bridge-standard-profiling`

### Performance Issues
- Monitor memory usage in VisualVM Monitor tab
- Use Sampler for CPU profiling
- Check thread states in Threads tab

## Stopping the Container

```shell
docker stop jdbc-bridge-standard-profiling
docker rm jdbc-bridge-standard-profiling
```

## Java Version Information

The container runs on:
- **Java Version**: OpenJDK 21.0.7 LTS
- **JVM**: Eclipse Temurin HotSpot
- **Garbage Collector**: G1GC (enabled by default)
- **Additional Features**: String deduplication, container support