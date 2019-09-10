# Тестовый чат

## Подготовка

Запускаем базу данных
```bash
docker container run --detach --publish 27017:27017 mongo
```

## Сборка приложения

```bash
gradle clean fatJar
```

## Запуск приложения

```bash
gradle run
```
or
```bash
java -jar build/libs/chat-fat-1.0-SNAPSHOT.jar
```

## Использование

### Клиент
[http://localhost:8082/](http://localhost:8082/)

### История сообщений
[http://localhost:8081/messages](http://localhost:8081/messages)

### Форма загрузки изображений
[http://localhost:8082/uploads.html](http://localhost:8082/uploads.html)

### Список загруженных изображений
[http://localhost:8081/images](http://localhost:8081/images)

### Скачать выбранное изображение по ID
`http://localhost:8081/images/<imageId>`
