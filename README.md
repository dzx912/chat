# Тестовый чат

## Подготовка
Запускаем базу данных
```bash
docker container run --detach --publish 27017:27017 mongo
```

## Запуск приложения
```bash
gradle run
```

## Сборка приложения
```bash
gradle clean fatJar
```
```bash
java -jar build/libs/chat-fat-1.0-SNAPSHOT.jar
```