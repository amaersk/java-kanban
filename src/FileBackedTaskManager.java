import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile, HistoryManager historyManager) {
        super(historyManager);
        this.saveFile = saveFile;
    }

    // Переопределяем методы для автосохранения

    @Override
    public void addNewTask(Task task) {
        super.addNewTask(task);
        save();
    }

    @Override
    public void addNewEpic(Epic epic) {
        super.addNewEpic(epic);
        save();
    }

    @Override
    public void addNewSubtask(Subtask subtask) {
        super.addNewSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void clearAllSubtasks() {
        super.clearAllSubtasks();
        save();
    }

    // Сохранение текущего состояния в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл", e);
        }
    }

    // Преобразование задачи в строку формата CSV
    private String toString(Task task) {
        return String.join(",",
                String.valueOf(task.getId()),
                Type.TASK.toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                ""
        );
    }

    private String toString(Epic epic) {
        return String.join(",",
                String.valueOf(epic.getId()),
                Type.EPIC.toString(),
                epic.getName(),
                epic.getStatus().toString(),
                epic.getDescription(),
                ""
        );
    }

    private String toString(Subtask subtask) {
        return String.join(",",
                String.valueOf(subtask.getId()),
                Type.SUBTASK.toString(),
                subtask.getName(),
                subtask.getStatus().toString(),
                subtask.getDescription(),
                String.valueOf(subtask.getIdEpic())
        );
    }

    // Загрузка данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, Managers.getDefaultHistory());

        if (!file.exists()) return manager; // если файла нет — возвращаем пустой менеджер

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return manager; // если файл пустой или содержит только заголовок

            // Читаем задачи
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) break; // Пропускаем пустые строки
                
                Task task = fromString(line);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            manager.epics.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            manager.subTasks.put(task.getId(), (Subtask) task);
                            break;
                    }
                }
            }

            // Восстанавливаем связи эпиков и подзадач
            for (Subtask subtask : manager.subTasks.values()) {
                Epic epic = manager.epics.get(subtask.getIdEpic());
                if (epic != null) {
                    epic.addIdSubtask(subtask.getId());
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла", e);
        }

        return manager;
    }

    // Парсинг строки в объект Task / Epic / Subtask
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        Type type = Type.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }
}