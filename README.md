# java-kanban
Repository for homework project.
amaersk@yandex.ru

# HTTP Task Server

HTTP API для управления задачами, эпиками и подзадачами.

## Требования

- Java 11 или выше
- Библиотека Gson для работы с JSON

## Компиляция

```bash
# Компиляция основных классов
javac -cp ".;lib/*" -sourcepath src src/*.java src/task/*.java

# Компиляция тестов
javac -cp ".;lib/*;src" -sourcepath "src;test" test/*.java
```

## Запуск сервера

```bash
java -cp ".;lib/*;src" HttpTaskServer
```

Сервер запустится на порту 8080.

## API Endpoints

### Задачи (Tasks)

- `GET /tasks` - получить все задачи
- `GET /tasks/{id}` - получить задачу по ID
- `POST /tasks` - создать новую задачу или обновить существующую
- `DELETE /tasks/{id}` - удалить задачу по ID

### Подзадачи (Subtasks)

- `GET /subtasks` - получить все подзадачи
- `GET /subtasks/{id}` - получить подзадачу по ID
- `POST /subtasks` - создать новую подзадачу или обновить существующую
- `DELETE /subtasks/{id}` - удалить подзадачу по ID

### Эпики (Epics)

- `GET /epics` - получить все эпики
- `GET /epics/{id}` - получить эпик по ID
- `GET /epics/{id}/subtasks` - получить подзадачи эпика
- `POST /epics` - создать новый эпик
- `DELETE /epics/{id}` - удалить эпик по ID

### История (History)

- `GET /history` - получить историю просмотров задач

### Приоритетные задачи (Prioritized)

- `GET /prioritized` - получить задачи в порядке приоритета (по времени начала)

## Коды ответов

- `200` - успешное выполнение запроса с возвратом данных
- `201` - успешное создание/обновление данных
- `404` - ресурс не найден
- `406` - задача пересекается с существующими по времени
- `500` - внутренняя ошибка сервера

## Формат данных

Все данные передаются в формате JSON.

### Пример задачи:

```json
{
  "id": 1,
  "name": "Задача 1",
  "description": "Описание задачи",
  "status": "NEW",
  "type": "TASK",
  "startTime": "2024-01-01T10:00:00",
  "duration": 60
}
```

## Тестирование

Для запуска тестов используйте JUnit 5. Убедитесь, что сервер остановлен перед запуском тестов.

## Структура проекта

- `src/` - исходный код
  - `task/` - классы задач, эпиков и подзадач
  - `HttpTaskServer.java` - HTTP сервер со всеми обработчиками
  - `TaskManager.java` - интерфейс менеджера задач
  - `InMemoryTaskManager.java` - реализация в памяти
  - `FileBackedTaskManager.java` - реализация с сохранением в файл
- `test/` - тесты
- `lib/` - библиотеки (Gson, JUnit)