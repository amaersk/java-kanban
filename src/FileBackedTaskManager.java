import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        super();
        this.saveFile = saveFile;
    }


    // Загрузка данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists()) return manager;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return manager;

            // Skip header line and process tasks
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty() || line.isBlank()) continue;

                Task task = fromString(line);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            Epic epic = (Epic) task;
                            manager.epics.put(epic.getId(), epic);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) task;
                            manager.subTasks.put(subtask.getId(), subtask);
                            // Add the subtask to its epic right away
                            Epic parentEpic = manager.epics.get(subtask.getIdEpic());
                            if (parentEpic != null) {
                                parentEpic.addIdSubtask(subtask.getId());
                            }
                            break;
                    }
                }
            }

            // Update epic statuses after all subtasks are loaded
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла", e);
        }

        return manager;
    }

    // Парсинг строки в объект Task / Epic / Subtask
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) return null;  // Minimum required fields

        try {
            int id = Integer.parseInt(parts[0]);
            Type type = Type.valueOf(parts[1]);
            String name = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];

            switch (type) {
                case TASK:
                    Task task = new Task(name, description, status);
                    task.setId(id);
                    return task;
                case EPIC:
                    Epic epic = new Epic(name, description, status);
                    epic.setId(id);
                    return epic;
                case SUBTASK:
                    if (parts.length < 6) return null;  // Subtask requires epic ID
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setId(id);
                    return subtask;
                default:
                    return null;
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;  // Return null if parsing fails
        }
    }

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
            // Write header
            writer.write("id,type,name,status,description,epic\n");

            // Write tasks
            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            // Write epics
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            // Write subtasks
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
        StringBuilder builder = new StringBuilder();
        builder.append(task.getId()).append(",");
        builder.append(task.getType()).append(",");
        builder.append(task.getName()).append(",");
        builder.append(task.getStatus()).append(",");
        builder.append(task.getDescription());

        if (task.getType() == Type.SUBTASK) {
            builder.append(",").append(((Subtask) task).getIdEpic());
        }

        return builder.toString();
    }
}