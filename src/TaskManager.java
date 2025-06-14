import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    //Создание задачи
    void addNewTask(Task task);

    //Создание эпиков
    void addNewEpic(Epic epic);

    //Создание подзадач
    void addNewSubtask(Subtask subtask);

    //Обновление задачи
    void updateTask(Task task);

    //Обновление эпика
    void updateEpic(Epic epic);

    //Обновление подзадачи
    void updateSubtask(Subtask subtask);

    //Получение задачи по id
    Task getTaskById(int id);

    //Получение эпик по id
    Epic getEpicById(int id);

    //Получение подзадачи по id
    Subtask getSubtaskById(int id);

    //Печать задач
    ArrayList<Task> getTasks();

    //Печать эпиков
    ArrayList<Epic> getEpics();

    //Печать подзадач
    ArrayList<Subtask> getSubtasks();

    //Получение списка всех подзадач определённого эпика по id
    ArrayList<Subtask> printArrayIdSubtask(int idEpic);

    //Удаление задач по ID
    void deleteTask(int id);

    //Удаление эпиков по ID
    void deleteEpic(int id);

    //Удаление подзадач по ID
    void deleteSubtask(int id);

    //Очистка всех задач
    void clearAllTasks();

    //Очистка всех эпиков и подзадач с ними
    void clearAllEpics();

    //Очистка всех подзадач и обновление статуса эпиков
    void clearAllSubtasks();

    //Получение истории просмотра всех задач
    List<Task> getHistory();

    //Получение списка задач, отсортированных по приоритету (startTime)
    Set<Task> getPrioritizedTasks();

    //Проверка пересечения двух задач по времени
    boolean isTasksIntersect(Task task1, Task task2);

    //Проверка пересечения задачи с любой другой задачей в менеджере
    boolean isTaskIntersectWithAny(Task task);
}
