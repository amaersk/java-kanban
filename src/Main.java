import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;


public class Main {

    public static void main(String[] args) {

        File file = new File("tasks.csv");
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager manager = new FileBackedTaskManager(file, historyManager);

        // Добавляем задачи
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        manager.addNewTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW);
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1", Status.NEW);
        manager.addNewEpic(epic1);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2", Status.NEW);
        manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", Status.NEW, epic1.getId());
        manager.addNewSubtask(subtask1);

        // Все изменения будут сохранены в файл

        // Проверим загрузку из файла
        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Все задачи после загрузки:");
        for (Task task : loadedManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Все эпики после загрузки:");
        for (Epic epic : loadedManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("Все сабтаски после загрузки:");
        for (Subtask subtask : loadedManager.getSubtasks()) {
            System.out.println(subtask);
        }
    }
}
