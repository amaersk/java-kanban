import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskTimeTest {
    private InMemoryTaskManager taskManager;
    private Task task1;
    private Task task2;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();

        // Создаем задачи с разным временем
        task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));

        task2 = new Task("Task 2", "Description 2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setDuration(Duration.ofMinutes(30));

        // Создаем эпик с подзадачами
        epic = new Epic("Epic 1", "Epic Description", Status.NEW);
        taskManager.addNewEpic(epic);

        subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", Status.NEW, epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        subtask1.setDuration(Duration.ofMinutes(45));

        subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", Status.NEW, epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask2.setDuration(Duration.ofMinutes(30));
    }

    @Test
    void testTaskIntersection() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        // Тестируем непересекающиеся задачи
        assertFalse(taskManager.isTasksIntersect(task1, task2));

        // Создаем пересекающуюся задачу
        Task intersectingTask = new Task("Intersecting Task", "Description", Status.NEW);
        intersectingTask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        intersectingTask.setDuration(Duration.ofMinutes(60));

        assertTrue(taskManager.isTasksIntersect(task1, intersectingTask));
        assertTrue(taskManager.isTaskIntersectWithAny(intersectingTask));
    }

    @Test
    void testPrioritizedTasks() {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description of Epic 1", Status.NEW);
        taskManager.addNewEpic(epic);

        // Создаем задачи с разным временем начала
        Task task1 = new Task("Task 1", "Description of Task 1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        taskManager.addNewTask(task1);

        Task task2 = new Task("Task 2", "Description of Task 2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setDuration(Duration.ofMinutes(30));
        taskManager.addNewTask(task2);

        // Создаем подзадачи
        Subtask subtask1 = new Subtask("Subtask 1", "Description of Subtask 1", Status.NEW, epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        subtask1.setDuration(Duration.ofMinutes(45));
        taskManager.addNewSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description of Subtask 2", Status.NEW, epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask2.setDuration(Duration.ofMinutes(30));
        taskManager.addNewSubtask(subtask2);

        // Получаем приоритетные задачи
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        // Преобразуем Set в List для удобства проверки
        List<Task> taskList = new ArrayList<>(prioritizedTasks);
        
        // Проверяем, что все задачи добавлены и отсортированы по времени начала
        assertEquals(4, taskList.size(), "Должно быть 4 задачи в списке");
        
        // Проверяем порядок задач
        assertEquals(subtask1, taskList.get(0), "Первой должна быть Подзадача 1");
        assertEquals(task1, taskList.get(1), "Второй должна быть Задача 1");
        assertEquals(subtask2, taskList.get(2), "Третьей должна быть Подзадача 2");
        assertEquals(task2, taskList.get(3), "Четвертой должна быть Задача 2");
    }

    @Test
    void testEpicTimeCalculation() {
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        // Время окончания эпика должно быть временем окончания последней подзадачи
        LocalDateTime expectedEndTime = subtask2.getStartTime().plus(subtask2.getDuration());
        assertEquals(expectedEndTime, epic.getEndTime());
    }

    @Test
    void testTaskWithoutTime() {
        Task taskWithoutTime = new Task("No Time Task", "Description", Status.NEW);
        taskManager.addNewTask(taskWithoutTime);

        // Задача без времени не должна быть в приоритетном списке
        assertFalse(taskManager.getPrioritizedTasks().contains(taskWithoutTime));

        // Задача без времени не должна пересекаться ни с одной задачей
        assertFalse(taskManager.isTaskIntersectWithAny(taskWithoutTime));
    }

    @Test
    void testTaskUpdateWithIntersection() {
        taskManager.addNewTask(task1);

        // Пытаемся обновить task1 временем, которое пересекается с самим собой
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        taskManager.updateTask(task1);

        // Обновление должно быть разрешено, так как это та же самая задача
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30), task1.getStartTime());
    }
} 