import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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

            // Пропускаем заголовок и обрабатываем задачи
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
                            // Добавляем подзадачу в эпик сразу
                            Epic parentEpic = manager.epics.get(subtask.getIdEpic());
                            if (parentEpic != null) {
                                parentEpic.addIdSubtask(subtask.getId());
                            }
                            break;
                    }
                }
            }

            // Обновляем статус эпика после добавления всех подзадач
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла", e);
        }

        return manager;
    }

    // Преобразование строки в объект Task / Epic / Subtask
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) return null;  // Минимум требуемых полей

        try {
            int id = Integer.parseInt(parts[0]);
            Type type = Type.valueOf(parts[1]);
            String name = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];

            Task task = null;
            switch (type) {
                case TASK:
                    task = new Task(name, description, status);
                    break;
                case EPIC:
                    task = new Epic(name, description, status);
                    break;
                case SUBTASK:
                    if (parts.length < 6) return null;  // Подзадача требует ID эпика
                    int epicId = Integer.parseInt(parts[5]);
                    task = new Subtask(name, description, status, epicId);
                    break;
            }

            if (task != null) {
                task.setId(id);

                // Парсим продолжительность, если она есть
                if (parts.length > 6 && !parts[6].equals("null")) {
                    task.setDuration(Duration.ofMinutes(Long.parseLong(parts[6])));
                }

                // Парсим время начала, если оно есть
                if (parts.length > 7 && !parts[7].equals("null")) {
                    task.setStartTime(LocalDateTime.parse(parts[7], formatter));
                }
            }

            return task;
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;  // Возвращаем null, если парсинг не удался
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
            // Записываем заголовок
            writer.write("id,type,name,status,description,epic,duration,startTime\n");

            // Записываем задачи
            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            // Записываем эпики
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            // Записываем подзадачи
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

        // Добавляем продолжительность
        builder.append(",").append(task.getDuration() != null ? task.getDuration().toMinutes() : "null");

        // Добавляем время начала
        builder.append(",").append(task.getStartTime() != null ? task.getStartTime().format(formatter) : "null");

        return builder.toString();
    }
}