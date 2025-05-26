import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    private HistoryManager historyManager;
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        taskManager = Managers.getDefault(historyManager);
    }

    @Test
    void shouldReturnTrueIfTaskAdded() {
        taskManager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        historyManager.add(taskManager.getTaskById(1));
        final List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void shouldReturnTrueIfTaskDeleted() {
        taskManager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        historyManager.add(taskManager.getTaskById(1));
        taskManager.deleteTask(1);
        final List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "История пустая.");
    }

    @Test
    void checkSizeHistoryIfTaskEquals() {
        Task task = new Task("Задача", "Первая задача", Status.NEW);
        final int sizeFromRequestHistoryShouldBe = 1;
        final int sizeForCheckRequestSize = 20;
        for (int i = 0; i <= sizeForCheckRequestSize; i++) {
            historyManager.add(task);
        }
        assertEquals(sizeFromRequestHistoryShouldBe, historyManager.getHistory().size());
    }
}