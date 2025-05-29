import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault(Managers.getDefaultHistory());


        Task task1 = new Task("Задача 1", "Первая задача", Status.NEW);
        taskManager.addNewTask(task1);
        Task task2 = new Task("Задача 2", "Вторая задача", Status.NEW);
        taskManager.addNewTask(task2);

        // эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Первый эпик", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Первая подзадача Эпика 1", Status.NEW, 3);
        taskManager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Вторая подзадача Эпика 1", Status.NEW, 3);
        taskManager.addNewSubtask(subtask2);

        // эпик с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Второй эпик", Status.NEW);
        taskManager.addNewEpic(epic2);
        Subtask subtask3 = new Subtask("Подзадача 3", "Первая подзадача Эпика 2", Status.NEW, 6);
        taskManager.addNewSubtask(subtask3);

        Task task3 = new Task("Задача 3", "Третья задача", Status.NEW);
        taskManager.addNewTask(task3);
        Task task4 = new Task("Задача 4", "Четвертая задача", Status.NEW);
        taskManager.addNewTask(task4);
        Task task5 = new Task("Задача 5", "Пятая задача", Status.NEW);
        taskManager.addNewTask(task5);
        Task task6 = new Task("Задача 6", "Шестая задача", Status.NEW);
        taskManager.addNewTask(task6);
        Task task7 = new Task("Задача 7", "Седьмая задача", Status.NEW);
        taskManager.addNewTask(task7);
        Task task8 = new Task("Задача 8", "Восьмая задача", Status.NEW);
        taskManager.addNewTask(task8);
        Task task9 = new Task("Задача 9", "Девятая задача", Status.NEW);
        taskManager.addNewTask(task9);
        Task task10 = new Task("Задача 10", "Десятая задача", Status.NEW);
        taskManager.addNewTask(task10);


        System.out.println("Список эпиков" + taskManager.getEpics());
        System.out.println("Список задач" + taskManager.getTasks());
        System.out.println("Список подзадач" + taskManager.getSubtasks());


        System.out.println("Обновление статуса задачи");
        Task taskTemp = taskManager.getTaskById(1);
        taskTemp.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskTemp);
        System.out.println(taskManager.getTaskById(1));

        System.out.println("Обновление статуса подзадачи");
        Subtask subtaskTemp = taskManager.getSubtaskById(4);
        subtaskTemp.setStatus(Status.DONE);
        taskManager.updateSubtask(subtaskTemp);
        System.out.println(taskManager.getSubtaskById(4));
        System.out.println(taskManager.getEpicById(3));

        System.out.println("Печать списка подзадач в эпике");
        System.out.println(taskManager.printArrayIdSubtask(3));

        System.out.println("История просмотра задач 1:");
        taskManager.getEpicById(3);
        taskManager.getSubtaskById(4);
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getSubtaskById(5);
        taskManager.getTaskById(1);
        System.out.println(taskManager.getHistory());

        System.out.println("История просмотра задач 2:");
        for (int i = 9; i <= 15; i++) {
            taskManager.getTaskById(i);
        }
        System.out.println(taskManager.getHistory());

        System.out.println("Удаление задачи 1");
        taskManager.deleteTask(1);
        System.out.println(taskManager.getTasks());

        System.out.println("Удаление эпика 1 со всем подзадачами");
        taskManager.deleteEpic(3);
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("Удаление всех задач");
        taskManager.clearAllTasks();
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getHistory());

    }
}
