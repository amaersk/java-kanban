import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subTasks;
    protected final HistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks;
    private int idCounter;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));
        this.idCounter = 0;
    }

    private int idCount() {
        return ++idCounter;
    }

    //Создание задачи
    @Override
    public void addNewTask(Task task) {
        if (isTaskIntersectWithAny(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующими задачами");
        }
        task.setId(idCount());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    //Создание эпиков
    @Override
    public void addNewEpic(Epic epic) {
        epic.setId(idCount());
        epics.put(epic.getId(), epic);
    }

    //Создание подзадач
    @Override
    public void addNewSubtask(Subtask subtask) {
        if (isTaskIntersectWithAny(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующими задачами");
        }
        subtask.setId(idCount());
        subTasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getIdEpic());
        if (epic != null) {
            epic.addIdSubtask(subtask.getId());
            updateEpicStatus(subtask.getIdEpic());
            updateEpicTime(subtask.getIdEpic());
        }
    }

    //Обновление статуса Epic после добавления/удаления подзадач
    protected void updateEpicStatus(int idEpic) {
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
                if (subtask.getStatus().equals(Status.DONE)) isSubTaskDone = true;
                if (subtask.getStatus().equals(Status.NEW)) isSubTaskNew = true;
            }
            if (isSubTaskDone && !isSubTaskNew) epics.get(idEpic).setStatus(Status.DONE);
            else if (isSubTaskNew && !isSubTaskDone) epics.get(idEpic).setStatus(Status.NEW);
            else epics.get(idEpic).setStatus(Status.IN_PROGRESS);

        }
    }

    //Обновление задачи
    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId()) && !isTaskIntersectWithAny(task)) {
            Task oldTask = tasks.get(task.getId());
            // Удаляем старую версию из приоритетного списка
            prioritizedTasks.remove(oldTask);

            tasks.put(task.getId(), task);

            // Добавляем новую версию в приоритетный список, если есть время начала
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
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
        if (subtask != null && subTasks.containsKey(subtask.getId()) && !isTaskIntersectWithAny(subtask)) {
            Subtask oldSubtask = subTasks.get(subtask.getId());
            // Удаляем старую версию из приоритетного списка
            prioritizedTasks.remove(oldSubtask);

            subTasks.put(subtask.getId(), subtask);

            // Добавляем новую версию в приоритетный список, если есть время начала
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }

            updateEpicStatus(subtask.getIdEpic());
            updateEpicTime(subtask.getIdEpic());
        }
    }

    //Получение задачи по id
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    //Получение эпик по id
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    //Получение подзадачи по id
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subTasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
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
        Epic epic = epics.get(idEpic);
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<Integer> idSubtaskArray = epic.getIdSubtaskArray();
        ArrayList<Subtask> subtaskArray = new ArrayList<>();
        for (int id : idSubtaskArray) {
            subtaskArray.add(subTasks.get(id));
        }
        return subtaskArray;
    }

    //Удаление задач по ID
    @Override
    public void deleteTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
        historyManager.remove(id);
        tasks.remove(id);
    }

    //Удаление эпиков по ID
    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            // Удаляем эпик из приоритетного списка
            prioritizedTasks.remove(epic);

            // Удаляем все подзадачи эпика из приоритетного списка
            for (Integer idSubtask : epic.getIdSubtaskArray()) {
                Subtask subtask = subTasks.get(idSubtask);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(idSubtask);
                subTasks.remove(idSubtask);
            }
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    //Удаление подзадач по ID
    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subTasks.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            int idEpic = subtask.getIdEpic();
            epics.get(idEpic).deleteEpicSubtask(id);
            updateEpicStatus(idEpic);
        }
        historyManager.remove(id);
        subTasks.remove(id);
    }

    //Очистка всех задач
    @Override
    public void clearAllTasks() {
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    //Очистка всех эпиков и подзадач с ними
    @Override
    public void clearAllEpics() {
        for (Epic epic : epics.values()) {
            prioritizedTasks.remove(epic);
            for (Integer idSubtask : epic.getIdSubtaskArray()) {
                Subtask subtask = subTasks.get(idSubtask);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
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
            prioritizedTasks.remove(subTask);
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

    @Override
    public Set<Task> getPrioritizedTasks() {
        return new TreeSet<>(prioritizedTasks);
    }

    @Override
    public boolean isTasksIntersect(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null || task1.getDuration() == null || task2.getDuration() == null) {
            return false;
        }

        LocalDateTime task1Start = task1.getStartTime();
        LocalDateTime task1End = task1.getEndTime();
        LocalDateTime task2Start = task2.getStartTime();
        LocalDateTime task2End = task2.getEndTime();

        // Задачи пересекаются, если:
        // 1. Одна задача начинается до того, как заканчивается другая И
        // 2. Другая задача начинается до того, как заканчивается первая И
        // 3. Они не начинаются в одно и то же время И
        // 4. Одна задача не начинается точно в момент окончания другой
        return !task1End.isBefore(task2Start) && !task2End.isBefore(task1Start) && !task1Start.equals(task2Start) && !task1End.equals(task2Start) && !task2End.equals(task1Start);
    }

    @Override
    public boolean isTaskIntersectWithAny(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        // Проверяем пересечение с обычными задачами
        for (Task existingTask : tasks.values()) {
            if (existingTask.getId() == task.getId()) {
                continue; // Пропускаем сравнение с самим собой
            }
            if (isTasksIntersect(task, existingTask)) {
                return true;
            }
        }

        // Проверяем пересечение с подзадачами
        for (Subtask existingSubtask : subTasks.values()) {
            if (existingSubtask.getId() == task.getId()) {
                continue; // Пропускаем сравнение с самим собой
            }
            if (isTasksIntersect(task, existingSubtask)) {
                return true;
            }
        }

        return false;
    }

    protected void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = printArrayIdSubtask(epicId);
        if (subtasks.isEmpty()) {
            epic.setEndTime(null);
            return;
        }

        LocalDateTime startTime = subtasks.stream().map(Subtask::getStartTime).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);

        LocalDateTime endTime = subtasks.stream().map(Subtask::getEndTime).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);

        epic.setEndTime(endTime);
    }
}
