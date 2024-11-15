import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    protected final HashMap<Integer, Task> taskHashMap = new HashMap<>();
    protected final HashMap<Integer, Epic> epicHashMap = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtaskHashMap = new HashMap<>();
    private int idCounter;

    public TaskManager() {
        this.idCounter = 0;
    }

    private int idCount() {
        return ++idCounter;
    }

    //Создание задачи
    public void addNewTask(Task task) {
        if (task != null) {
            int idTask = idCount();
            task.setId(idTask); //дублируем id в класс Таск
            taskHashMap.put(idTask, task);
        }
    }

    //Создание эпиков
    public void addNewEpic(Epic epic) {
        if (epic != null) {
            int idEpic = idCount();
            epic.setId(idEpic);
            epicHashMap.put(idEpic, epic);
        }
    }

    //Создание подзадач
    public void addNewSubtask(Subtask subtask) {
        if (subtask != null) {
            int subTaskId = idCount();
            subtask.setId(subTaskId);
            subtaskHashMap.put(subTaskId, subtask);

            int idEpic = subtask.getIdEpic();
            Epic epic = epicHashMap.get(idEpic);
            epic.addIdSubtask(subTaskId);//добавили в эпик id подзадачи
            updateEpicStatus(idEpic);
        }
    }

    //Обновление статуса Epic после добавления/удаления подзадач
    private void updateEpicStatus(int idEpic) {
        Epic epic = epicHashMap.get(idEpic);
        ArrayList<Integer> iDSubtaskArray = epic.getIdSubtaskArray();
        ArrayList<Subtask> subtasksArray = new ArrayList<>();
        for (int id : iDSubtaskArray) {
            subtasksArray.add(subtaskHashMap.get(id));
        }
        boolean isSubTaskNew = false;
        boolean isSubTaskDone = false;
        if (iDSubtaskArray.isEmpty()) { //если у эпика нет подзадач, то статус должен быть NEW
            epicHashMap.get(idEpic).setStatus(Status.NEW);
        } else {
            for (Subtask subtask : subtasksArray) {
                if (subtask.getStatus().equals(Status.DONE))
                    isSubTaskDone = true;
                if (subtask.getStatus().equals(Status.NEW))
                    isSubTaskNew = true;
            }
            if (isSubTaskDone && !isSubTaskNew)
                epicHashMap.get(idEpic).setStatus(Status.DONE);
            else if (isSubTaskNew && !isSubTaskDone)
                epicHashMap.get(idEpic).setStatus(Status.NEW);
            else epicHashMap.get(idEpic).setStatus(Status.IN_PROGRESS);

        }
    }


    //Обновление задачи
    public void updateTask(Task task) {
        if (task != null && taskHashMap.containsKey(task.getId())) {
            taskHashMap.put(task.getId(), task);
        }
    }

    //Обновление эпика
    public void updateEpic(Epic epic) {
        if (epic != null && epicHashMap.containsKey(epic.getId())) {
            epicHashMap.put(epic.getId(), epic);
        }
    }

    //Обновление подзадачи
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subtaskHashMap.containsKey(subtask.getId())) {
            subtaskHashMap.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getIdEpic());
        }
    }


    //Получение задачи по id
    public Task getTaskById(int id) {
        return taskHashMap.get(id);
    }

    //Получение эпик по id
    public Epic getEpicById(int id) {
        return epicHashMap.get(id);
    }

    //Получение подзадачи по id
    public Subtask getSubtaskById(int id) {
        return subtaskHashMap.get(id);
    }


    //Печать задач
    public ArrayList<Task> printAllTasks() {
        if (taskHashMap.isEmpty()) {
            System.out.println("Задачи отсутствуют!");
        }
        return new ArrayList<>(taskHashMap.values());
    }

    //Печать эпиков
    public ArrayList<Epic> printAllEpics() {
        if (epicHashMap.isEmpty()) {
            System.out.println("Эпики отсутствуют!");
        }
        return new ArrayList<>(epicHashMap.values());
    }

    //Печать подзадач
    public ArrayList<Subtask> printAllSubtask() {
        if (subtaskHashMap.isEmpty()) {
            System.out.println("Подзадачи отсутствуют!");
        }
        return new ArrayList<>(subtaskHashMap.values());
    }

    //Получение списка всех подзадач определённого эпика по id
    public ArrayList<Subtask> printArrayIdSubtask(int idEpic) {
        ArrayList<Integer> idSubtaskArray = epicHashMap.get(idEpic).getIdSubtaskArray();
        ArrayList<Subtask> subtaskArray = new ArrayList<>();
        for (int id : idSubtaskArray) {
            subtaskArray.add(subtaskHashMap.get(id));
        }
        return subtaskArray;
    }


    //Удаление задач по ID
    public void deleteTask(int id) {
        taskHashMap.remove(id);
    }

    //Удаление эпиков по ID
    public void deleteEpic(int id) {
        for (Integer idSubtask : epicHashMap.get(id).getIdSubtaskArray()) {
            subtaskHashMap.remove(idSubtask);
        }
        epicHashMap.remove(id);
    }

    //Удаление подзадач по ID
    public void deleteSubtask(int id) {
        int idEpic = subtaskHashMap.get(id).getIdEpic();
        epicHashMap.get(idEpic).deleteEpicSubtask(id);
        subtaskHashMap.remove(id);
        updateEpicStatus(idEpic);
    }

    //Очистка всех задач
    public void clearAllTasks() {
        taskHashMap.clear();
    }

    //Очистка всех эпиков и подзадач с ними
    public void clearAllEpics() {
        epicHashMap.clear();
        subtaskHashMap.clear();
    }

    //Очистка всех подзадач и обновление статуса эпиков
    public void clearAllSubtasks() {
        subtaskHashMap.clear();
        for (Epic epic : epicHashMap.values()) {
            epic.clearSubtaskArray();
            epic.setStatus(Status.NEW);
        }
    }


}
