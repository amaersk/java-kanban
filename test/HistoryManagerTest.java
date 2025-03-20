import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class HistoryManagerTest {



    @Test
    void shouldReturnTrueIfTaskAdded() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager manager = Managers.getDefault(historyManager);
        manager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        historyManager.add(manager.getTaskById(1));
        final List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void shouldReturnTrueIfTaskDeleted() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager manager = Managers.getDefault(historyManager);
        manager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        historyManager.add(manager.getTaskById(1));
        manager.deleteTask(1);
        final List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "История пустая.");
    }

    @Test
    void checkSizeHistoryIfTaskEquals() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Задача", "Первая задача", Status.NEW);
        final int sizeFromRequestHistoryShouldBe = 1;
        final int sizeForCheckRequestSize = 20;
        for (int i = 0; i <= sizeForCheckRequestSize; i++) {
            historyManager.add(task);
        }
        assertEquals(sizeFromRequestHistoryShouldBe, historyManager.getHistory().size());
    }
}