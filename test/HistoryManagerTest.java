import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldReturnTrueIfTaskAdded() {
        taskManager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        taskManager.getTaskById(1);
        final List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void shouldReturnTrueIfTaskDeleted() {
        taskManager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        taskManager.getTaskById(1);
        taskManager.deleteTask(1);
        final List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "История пустая.");
    }

    @Test
    void checkSizeHistoryIfTaskEquals() {
        Task task = new Task("Задача", "Первая задача", Status.NEW);
        taskManager.addNewTask(task);
        final int sizeFromRequestHistoryShouldBe = 1;
        final int sizeForCheckRequestSize = 20;
        for (int i = 0; i <= sizeForCheckRequestSize; i++) {
            taskManager.getTaskById(task.getId());
        }
        assertEquals(sizeFromRequestHistoryShouldBe, taskManager.getHistory().size());
    }
}