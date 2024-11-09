import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();


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


        System.out.println("Список эпиков" + taskManager.printAllEpics());
        System.out.println("Список задач" + taskManager.printAllTasks());
        System.out.println("Список подзадач" + taskManager.printAllSubtask());


        //System.out.println("Список подзадач по Id эпика "+ taskManager.printArrayIdSubtask(3));
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

        System.out.println("Удаление задачи 1");
        taskManager.deleteTask(1);
        System.out.println(taskManager.printAllTasks());

        System.out.println("Удаление эпика 1 со всем поздачами");
        taskManager.deleteEpic(3);
        System.out.println(taskManager.printAllEpics());
        System.out.println(taskManager.printAllSubtask());

        System.out.println("Удаление всех задач");
        taskManager.clearAllTasks();
        System.out.println(taskManager.printAllTasks());


    }
}
