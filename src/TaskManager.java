import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

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
    ArrayList<Task> printAllTasks();

    //Печать эпиков
    ArrayList<Epic> printAllEpics();

    //Печать подзадач
    ArrayList<Subtask> printAllSubtask();

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
}
