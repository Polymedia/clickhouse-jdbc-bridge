#
# Copyright (C) 2019-2022, Zhichun Wu
# Copyright (C) 2023-2025, Visiology
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# Dockerfile for Standard JAR Build with Java 21 LTS
# Multi-stage build for ClickHouse JDBC Bridge

# Stage 1: Build the application with standard profile
FROM maven:3.9-eclipse-temurin-21 as builder

LABEL build.stage="builder"
LABEL build.profile="standard"

# Build argument to control test execution
ARG SKIP_TESTS=false

COPY LICENSE NOTICE pom.xml /app/
COPY docker /app/docker/
COPY misc /app/misc/
COPY src /app/src/

WORKDIR /app

# Build with standard profile (creates regular JAR files)
# Tests are executed by default, can be skipped with build arg
RUN if [ "$SKIP_TESTS" = "true" ]; then \
        mvn clean package -DskipTests; \
    else \
        mvn clean package; \
    fi

# Stage 2: Runtime environment
FROM eclipse-temurin:21.0.7_6-jre

ARG revision=latest

# Maintainer and labels
LABEL maintainer="Visiology"
LABEL app.name="ClickHouse JDBC Bridge"
LABEL app.version="${revision}"
LABEL build.profile="standard"
LABEL java.version="21-LTS"
LABEL runtime.type="JVM"

# Environment variables
ENV JDBC_BRIDGE_HOME=/app \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError"

# Update system and install additional packages for troubleshooting
RUN apt-get update \
	&& DEBIAN_FRONTEND=noninteractive apt-get install -y --allow-unauthenticated apache2-utils \
		apt-transport-https curl htop iftop iptraf iputils-ping jq lsof net-tools tzdata wget \
	&& apt-get clean \
	&& mkdir -p $JDBC_BRIDGE_HOME/drivers \
	&& wget -P $JDBC_BRIDGE_HOME/drivers \
		https://repo1.maven.org/maven2/com/clickhouse/clickhouse-jdbc/0.9.0/clickhouse-jdbc-0.9.0-all.jar \
		https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.0.4/mariadb-java-client-3.0.4.jar \
		https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar \
		https://repo1.maven.org/maven2/org/neo4j/neo4j-jdbc-full-bundle/6.6.0/neo4j-jdbc-full-bundle-6.6.0.jar \
		https://repo1.maven.org/maven2/com/amazon/opendistroforelasticsearch/client/opendistro-sql-jdbc/1.13.0.0/opendistro-sql-jdbc-1.13.0.0.jar \
		https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar \
		https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar \
		https://repo1.maven.org/maven2/io/trino/trino-jdbc/377/trino-jdbc-377.jar \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Copy configuration files
COPY --chown=root:root docker/ $JDBC_BRIDGE_HOME

# Copy built application from builder stage
COPY --from=builder /app/LICENSE /app/NOTICE $JDBC_BRIDGE_HOME/
COPY --from=builder /app/target/clickhouse-jdbc-bridge-*-shaded.jar $JDBC_BRIDGE_HOME/clickhouse-jdbc-bridge-shaded.jar

RUN chmod +x $JDBC_BRIDGE_HOME/*.sh \
    && mkdir -p $JDBC_BRIDGE_HOME/logs /usr/local/lib/java \
    && ln -s $JDBC_BRIDGE_HOME/logs /var/log/clickhouse-jdbc-bridge \
    && ln -s $JDBC_BRIDGE_HOME/clickhouse-jdbc-bridge-shaded.jar \
        /usr/local/lib/java/clickhouse-jdbc-bridge-shaded.jar \
    && ln -s $JDBC_BRIDGE_HOME /etc/clickhouse-jdbc-bridge

WORKDIR $JDBC_BRIDGE_HOME

EXPOSE 9019

VOLUME ["${JDBC_BRIDGE_HOME}/drivers", "${JDBC_BRIDGE_HOME}/extensions", "${JDBC_BRIDGE_HOME}/logs", "${JDBC_BRIDGE_HOME}/scripts"]

CMD ["./docker-entrypoint.sh"] 