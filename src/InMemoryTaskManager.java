import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subTasks;
    protected final HistoryManager historyManager;
    private int idCounter;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = historyManager;
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
            tasks.put(idTask, task);
        }
    }

    //Создание эпиков
    @Override
    public void addNewEpic(Epic epic) {
        if (epic != null) {
            int idEpic = idCount();
            epic.setId(idEpic);
            epics.put(idEpic, epic);
        }
    }

    //Создание подзадач
    @Override
    public void addNewSubtask(Subtask subtask) {
        Epic epicCheck = epics.get(subtask.getIdEpic());

        if (epicCheck != null) {
            int subTaskId = idCount();
            subtask.setId(subTaskId);
            subTasks.put(subTaskId, subtask);

            int idEpic = subtask.getIdEpic();
            Epic epic = epics.get(idEpic);
            epic.addIdSubtask(subTaskId);//добавили в эпик id подзадачи
            updateEpicStatus(idEpic);
        }
    }

    //Обновление статуса Epic после добавления/удаления подзадач
    private void updateEpicStatus(int idEpic) {
        Epic epic = epics.get(idEpic);
        ArrayList<Integer> iDSubtaskArray = epic.getIdSubtaskArray();
        ArrayList<Subtask> subtasksArray = new ArrayList<>();
        for (int id : iDSubtaskArray) {
            subtasksArray.add(subTasks.get(id));
        }
        boolean isSubTaskNew = false;
        boolean isSubTaskDone = false;
        if (iDSubtaskArray.isEmpty()) { //если у эпика нет подзадач, то статус должен быть NEW
            epics.get(idEpic).setStatus(Status.NEW);
        } else {
            for (Subtask subtask : subtasksArray) {
                if (subtask.getStatus().equals(Status.DONE))
                    isSubTaskDone = true;
                if (subtask.getStatus().equals(Status.NEW))
                    isSubTaskNew = true;
            }
            if (isSubTaskDone && !isSubTaskNew)
                epics.get(idEpic).setStatus(Status.DONE);
            else if (isSubTaskNew && !isSubTaskDone)
                epics.get(idEpic).setStatus(Status.NEW);
            else epics.get(idEpic).setStatus(Status.IN_PROGRESS);

        }
    }


    //Обновление задачи
    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    //Обновление эпика
    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    //Обновление подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subTasks.containsKey(subtask.getId())) {
            subTasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getIdEpic());
        }
    }


    //Получение задачи по id
    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    //Получение эпик по id
    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    //Получение подзадачи по id
    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }


    //Печать задач
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    //Печать эпиков
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    //Печать подзадач
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subTasks.values());
    }

    //Получение списка всех подзадач определённого эпика по id
    @Override
    public ArrayList<Subtask> printArrayIdSubtask(int idEpic) {
        ArrayList<Integer> idSubtaskArray = epics.get(idEpic).getIdSubtaskArray();
        ArrayList<Subtask> subtaskArray = new ArrayList<>();
        for (int id : idSubtaskArray) {
            subtaskArray.add(subTasks.get(id));
        }
        return subtaskArray;
    }


    //Удаление задач по ID
    @Override
    public void deleteTask(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    //Удаление эпиков по ID
    @Override
    public void deleteEpic(int id) {
        for (Integer idSubtask : epics.get(id).getIdSubtaskArray()) {
            historyManager.remove(idSubtask);
            subTasks.remove(idSubtask);
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    //Удаление подзадач по ID
    @Override
    public void deleteSubtask(int id) {
        int idEpic = subTasks.get(id).getIdEpic();
        epics.get(idEpic).deleteEpicSubtask(id);
        historyManager.remove(id);
        subTasks.remove(id);
        updateEpicStatus(idEpic);
    }

    //Очистка всех задач
    @Override
    public void clearAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    //Очистка всех эпиков и подзадач с ними
    @Override
    public void clearAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer idSubtask : epic.getIdSubtaskArray()) {
                historyManager.remove(idSubtask);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subTasks.clear();
    }

    //Очистка всех подзадач и обновление статуса эпиков
    @Override
    public void clearAllSubtasks() {

        for (Subtask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskArray();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


}
