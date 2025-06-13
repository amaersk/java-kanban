import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subTasks;
    protected final HistoryManager historyManager;
    private int idCounter;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.idCounter = 0;
    }

    private int idCount() {
        return ++idCounter;
    }

    //Создание задачи
    @Override
    public void addNewTask(Task task) {
        System.out.println("\nПопытка добавить задачу: " + task.getName());
        if (isTaskIntersectWithAny(task)) {
            System.out.println("Не удалось добавить задачу: " + task.getName() + " из-за пересечения по времени");
            return;
        }
        task.setId(idCount());
        tasks.put(task.getId(), task);
        System.out.println("Успешно добавлена задача: " + task.getName() + " с ID: " + task.getId());
    }

    //Создание эпиков
    @Override
    public void addNewEpic(Epic epic) {
        epic.setId(idCount());
        epics.put(epic.getId(), epic);
        System.out.println("Добавлен эпик: " + epic.getName() + " с ID: " + epic.getId());
    }

    //Создание подзадач
    @Override
    public void addNewSubtask(Subtask subtask) {
        System.out.println("\nПопытка добавить подзадачу: " + subtask.getName());
        if (isTaskIntersectWithAny(subtask)) {
            System.out.println("Не удалось добавить подзадачу: " + subtask.getName() + " из-за пересечения по времени");
            return;
        }
        subtask.setId(idCount());
        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getIdEpic());
        if (epic != null) {
            epic.addIdSubtask(subtask.getId());
            updateEpicStatus(subtask.getIdEpic());
            updateEpicTime(subtask.getIdEpic());
        }
        System.out.println("Успешно добавлена подзадача: " + subtask.getName() + " с ID: " + subtask.getId());
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
        if (task != null && tasks.containsKey(task.getId()) && !isTaskIntersectWithAny(task)) {
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
        if (subtask != null && subTasks.containsKey(subtask.getId()) && !isTaskIntersectWithAny(subtask)) {
            subTasks.put(subtask.getId(), subtask);
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

    @Override
    public Set<Task> getPrioritizedTasks() {
        // Создаем TreeSet с компаратором, который:
        // 1. Сначала сравнивает по времени начала (null значения идут в конец)
        // 2. При равном времени начала сравнивает по ID
        TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
            if (t1.getStartTime() == null && t2.getStartTime() == null) {
                return Integer.compare(t1.getId(), t2.getId());
            }
            if (t1.getStartTime() == null) {
                return 1;
            }
            if (t2.getStartTime() == null) {
                return -1;
            }
            int timeCompare = t1.getStartTime().compareTo(t2.getStartTime());
            return timeCompare != 0 ? timeCompare : Integer.compare(t1.getId(), t2.getId());
        });

        // Добавляем все задачи с ненулевым временем начала
        for (Task task : tasks.values()) {
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }

        // Добавляем все подзадачи с ненулевым временем начала
        for (Subtask subtask : subTasks.values()) {
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
        }

        // Добавляем все эпики с ненулевым временем начала
        for (Epic epic : epics.values()) {
            if (epic.getStartTime() != null) {
                prioritizedTasks.add(epic);
            }
        }

        return prioritizedTasks;
    }

    @Override
    public boolean isTasksIntersect(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
            task1.getDuration() == null || task2.getDuration() == null) {
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
        return !task1End.isBefore(task2Start) && 
               !task2End.isBefore(task1Start) && 
               !task1Start.equals(task2Start) &&
               !task1End.equals(task2Start) &&
               !task2End.equals(task1Start);
    }

    @Override
    public boolean isTaskIntersectWithAny(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        System.out.println("\nПроверка пересечений для задачи: " + task.getName());
        System.out.println("Время начала: " + task.getStartTime());
        System.out.println("Продолжительность: " + task.getDuration());
        System.out.println("Время окончания: " + task.getEndTime());

        // Проверяем пересечение с обычными задачами
        for (Task existingTask : tasks.values()) {
            if (existingTask.getId() == task.getId()) {
                continue; // Пропускаем сравнение с самим собой
            }
            if (isTasksIntersect(task, existingTask)) {
                System.out.println("Пересекается с: " + existingTask.getName());
                System.out.println("Время начала существующей задачи: " + existingTask.getStartTime());
                System.out.println("Время окончания существующей задачи: " + existingTask.getEndTime());
                System.out.println("Есть пересечение: true");
                return true;
            }
        }

        // Проверяем пересечение с подзадачами
        for (Subtask existingSubtask : subTasks.values()) {
            if (existingSubtask.getId() == task.getId()) {
                continue; // Пропускаем сравнение с самим собой
            }
            if (isTasksIntersect(task, existingSubtask)) {
                System.out.println("Пересекается с: " + existingSubtask.getName());
                System.out.println("Время начала существующей задачи: " + existingSubtask.getStartTime());
                System.out.println("Время окончания существующей задачи: " + existingSubtask.getEndTime());
                System.out.println("Есть пересечение: true");
                return true;
            }
        }

        System.out.println("Есть пересечение: false");
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

        LocalDateTime startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setEndTime(endTime);
    }
}
