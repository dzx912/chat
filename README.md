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

## Использование
### Клиент
[http://localhost:8082/](http://localhost:8082/)

### История сообщений
[http://localhost:8081/getHistory](http://localhost:8081/getHistory)


## Решение тестового задания
Добавлено 2 REST метода для работы с изображениями:
* загрузка изображения/й:
```
POST localhost:8081/uploadImage
```  
* получение изображения:
```
localhost:8081/getImage/{image_id}
```

### Загрузка:
На сервер можно загружать одно или несколько изображений. Это осуществляется в частности за счет метода `fileUploads()` в `RestServerVerticle`. 
Изображения сначала закачиваются на сервер (в ./uploads), а затем сохраняются в базе методом `saveImage` в `MongoDbVerticle`.
В случае одновременной загрузки нескольких изображений используется метод `saveMultipleImages`.

Фаилы хранятся в mongoDb как Binary в формате base-64:
![image](https://user-images.githubusercontent.com/33380175/64965409-9c993300-d8a5-11e9-95b8-0dc65b934919.png)

После загрузки изображения на сервер, получаем ID документа в качестве ответа.
Пример на Postman:
![image](https://user-images.githubusercontent.com/33380175/64965929-8770d400-d8a6-11e9-9560-7b1cf5ac3e63.png)

* Примечание: в этом примере при загрузке изображения через Postman используется form-data в качестве request body

### Получение:
Метод `/getImage` в `RestServerVerticle` возвращает изображение по ID документа.
ID передаётся в `MongoDbVerticle` по EventBus и от туда возвращается JsonObject с данными изображения в формате base-64.
Эти данные в итоге возвращаются клиенту.

![image](https://user-images.githubusercontent.com/33380175/64967484-43330300-d8a9-11e9-9f34-238ee56c7b88.png)

* Примечание: на стороне клиента можно будет визуализировать полученный контент, например в HTML с Thymeleaf:
```
<br/>
	<img alt="sample" th:src="*{'data:image/png;base64,'+image}" width="200"/>
<br/> <br/>
```
* Для де-кодирования base-64 -> png/jpeg можно использовать https://onlinejpgtools.com/convert-base64-to-jpg

### Примечания:
В процессе решения были главными следующие принципы: 
* Не изменять уже существующй код
* Использовать существующую архитектуру
* Не использовать дополнительных фрэймворков или библиотек воизбежание тяжеловесности проекта
