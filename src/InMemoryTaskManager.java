import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> taskHashMap = new HashMap<>();
    protected final HashMap<Integer, Epic> epicHashMap = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtaskHashMap = new HashMap<>();
    public HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter;

    public InMemoryTaskManager() {
        this.idCounter = 0;
    }

    private int idCount() {
        return ++idCounter;
    }

    //Создание задачи
    @Override
    public void addNewTask(Task task) {
        if (task != null) {
            int idTask = idCount();
            task.setId(idTask); //дублируем id в класс Таск
            taskHashMap.put(idTask, task);
        }
    }

    //Создание эпиков
    @Override
    public void addNewEpic(Epic epic) {
        if (epic != null) {
            int idEpic = idCount();
            epic.setId(idEpic);
            epicHashMap.put(idEpic, epic);
        }
    }

    //Создание подзадач
    @Override
    public void addNewSubtask(Subtask subtask) {
        Epic epicCheck = epicHashMap.get(subtask.getIdEpic());

        if (epicCheck != null) {
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
    @Override
    public void updateTask(Task task) {
        if (task != null && taskHashMap.containsKey(task.getId())) {
            taskHashMap.put(task.getId(), task);
        }
    }

    //Обновление эпика
    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && epicHashMap.containsKey(epic.getId())) {
            epicHashMap.put(epic.getId(), epic);
        }
    }

    //Обновление подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subtaskHashMap.containsKey(subtask.getId())) {
            subtaskHashMap.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getIdEpic());
        }
    }


    //Получение задачи по id
    @Override
    public Task getTaskById(int id) {
        historyManager.add(taskHashMap.get(id));
        return taskHashMap.get(id);
    }

    //Получение эпик по id
    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epicHashMap.get(id));
        return epicHashMap.get(id);
    }

    //Получение подзадачи по id
    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtaskHashMap.get(id));
        return subtaskHashMap.get(id);
    }


    //Печать задач
    @Override
    public ArrayList<Task> printAllTasks() {
        if (taskHashMap.isEmpty()) {
            System.out.println("Задачи отсутствуют!");
        }
        return new ArrayList<>(taskHashMap.values());
    }

    //Печать эпиков
    @Override
    public ArrayList<Epic> printAllEpics() {
        if (epicHashMap.isEmpty()) {
            System.out.println("Эпики отсутствуют!");
        }
        return new ArrayList<>(epicHashMap.values());
    }

    //Печать подзадач
    @Override
    public ArrayList<Subtask> printAllSubtask() {
        if (subtaskHashMap.isEmpty()) {
            System.out.println("Подзадачи отсутствуют!");
        }
        return new ArrayList<>(subtaskHashMap.values());
    }

    //Получение списка всех подзадач определённого эпика по id
    @Override
    public ArrayList<Subtask> printArrayIdSubtask(int idEpic) {
        ArrayList<Integer> idSubtaskArray = epicHashMap.get(idEpic).getIdSubtaskArray();
        ArrayList<Subtask> subtaskArray = new ArrayList<>();
        for (int id : idSubtaskArray) {
            subtaskArray.add(subtaskHashMap.get(id));
        }
        return subtaskArray;
    }


    //Удаление задач по ID
    @Override
    public void deleteTask(int id) {
        taskHashMap.remove(id);
    }

    //Удаление эпиков по ID
    @Override
    public void deleteEpic(int id) {
        for (Integer idSubtask : epicHashMap.get(id).getIdSubtaskArray()) {
            subtaskHashMap.remove(idSubtask);
        }
        epicHashMap.remove(id);
    }

    //Удаление подзадач по ID
    @Override
    public void deleteSubtask(int id) {
        int idEpic = subtaskHashMap.get(id).getIdEpic();
        epicHashMap.get(idEpic).deleteEpicSubtask(id);
        subtaskHashMap.remove(id);
        updateEpicStatus(idEpic);
    }

    //Очистка всех задач
    @Override
    public void clearAllTasks() {
        taskHashMap.clear();
    }

    //Очистка всех эпиков и подзадач с ними
    @Override
    public void clearAllEpics() {
        epicHashMap.clear();
        subtaskHashMap.clear();
    }

    //Очистка всех подзадач и обновление статуса эпиков
    @Override
    public void clearAllSubtasks() {
        subtaskHashMap.clear();
        for (Epic epic : epicHashMap.values()) {
            epic.clearSubtaskArray();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


}
