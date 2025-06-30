# ClickHouse JDBC Bridge Utilities

Набор утилит для работы с проектом через Docker без необходимости локальной установки Maven.

## 🚀 Требования

- **Docker** - для выполнения команд Maven
- Находиться в корневой директории проекта (где находится `pom.xml`)

## 📋 Maven Dependencies Analysis

### Краткий анализ зависимостей (первые 20 строк)

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile | head -20"
```

### Полный анализ зависимостей

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dscope=compile
```

### Анализ всех scope

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree
```

### Детальный анализ с информацией о конфликтах

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dverbose=true
```

### Сохранить результаты в файл

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 sh -c "mvn dependency:tree -Dscope=compile > dependency-tree.txt"
```

### Найти все зависимости Vert.x

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dscope=compile | grep "io.vertx"
```

### Найти конфликты зависимостей

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn dependency:tree -Dverbose=true | grep -E "(conflict|omitted)"
```

## 📄 NOTICE File Update

### Автоматическое обновление файла NOTICE

```bash
docker run --rm -v ${PWD}:/usr/src/app -w /usr/src/app maven:3.9-eclipse-temurin-21 mvn org.jasig.maven:maven-notice-plugin:generate
```

## 🐳 Docker Build Commands

### Сборка Docker образа

```bash
docker build -t clickhouse-jdbc-bridge:latest .
```

### Запуск контейнера

```bash
docker run -d -p 9019:9019 --name jdbc-bridge clickhouse-jdbc-bridge:latest
```

### Запуск с JMX профилированием

```bash
docker run -d -p 9019:9019 -p 9999:9999 --name jdbc-bridge-profiling \
  -e JDBC_BRIDGE_JVM_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
  clickhouse-jdbc-bridge:latest
```

### Отладочный запуск с bash

```bash
docker run -it --entrypoint /bin/bash clickhouse-jdbc-bridge:latest
```

## 🔍 Проверка и тестирование

### Сканирование уязвимостей Docker образа

```bash
docker run -i --rm -v /var/run/docker.sock:/var/run/docker.sock:ro -u 0 -v /tmp/trivy/:/tmp/trivy/ aquasec/trivy:0.62.1 image clickhouse-jdbc-bridge:latest --scanners vuln --timeout 30m -f table --cache-dir /tmp/trivy/ --severity HIGH,CRITICAL
```

### Проверка состояния приложения

```bash
curl http://localhost:9019/ping
```

### Проверка JMX порта

```bash
telnet localhost 9999
```

### Проверка логов контейнера

```bash
docker logs jdbc-bridge
```

### Мониторинг ресурсов контейнера

```bash
docker stats jdbc-bridge
```

### Проверка процессов в контейнере

```bash
docker exec jdbc-bridge ps aux
```

## 🔧 Остановка контейнера

```bash
docker stop jdbc-bridge
docker rm jdbc-bridge
```

## 📊 Что проверять в выводе анализа зависимостей

### ✅ Корректная работа BOM:
- Все модули `io.vertx:*` имеют одинаковую версию (например, `4.5.1`)
- Нет строк с `(omitted for conflict)` для критических зависимостей
- BOM успешно управляет версиями модулей Vert.x

### ❌ Проблемы с BOM:
- Разные версии модулей Vert.x
- Конфликты между зависимостями `io.vertx:*`
- Неожиданные транзитивные зависимости

### Ожидаемый результат:
```
[INFO] +- io.vertx:vertx-core:jar:4.5.1:compile
[INFO] +- io.vertx:vertx-config:jar:4.5.1:compile
[INFO] +- io.vertx:vertx-web:jar:4.5.1:compile
[INFO] +- io.vertx:vertx-web-client:jar:4.5.1:compile
```

## 🔄 Рабочий процесс

1. **Изменили pom.xml** → проверить зависимости
2. **Обновили версии** → запустить детальный анализ  
3. **Перед коммитом** → обновить файл NOTICE
4. **При проблемах** → сохранить результаты в файл для детального анализа

## 🎯 JMX Профилирование с VisualVM

### Настройка соединения в VisualVM:
1. Запустить VisualVM 2.2+
2. Правый клик на "Remote" → "Add JMX Connection..."
3. Connection: `localhost:9999`
4. Display name: `ClickHouse JDBC Bridge (Java 21)`
5. Снять галочку "Use security credentials"
6. Подключиться

### Доступные возможности мониторинга:
- **Overview**: Версия JVM, системные свойства, аргументы JVM
- **Monitor**: Использование CPU, памяти, загруженные классы, потоки
- **Threads**: Состояния потоков и стеки вызовов
- **Sampler**: Профилирование CPU и памяти
- **MBeans**: JMX management beans для детального мониторинга

## 📝 Примечания

- Все команды используют официальный образ Maven с Java 21
- Требуется установленный Docker
- Выполнение происходит в контексте текущей директории проекта
- Образ использует Eclipse Temurin 21 JRE для долгосрочной поддержки 