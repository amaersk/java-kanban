import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TaskManagerTest {

    TaskManager manager = Managers.getDefault();

    @AfterEach
    void afterEach() {
        manager.clearAllTasks();
    }


    @Test
    public void shouldReturnTrueByEqualIdTask() {
        manager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        Task task1 = manager.getTaskById(1);
        Task task2 = manager.getTaskById(1);
        Assertions.assertEquals(task1, task2);
    }

    @Test
    public void shouldReturnTrueByEqualIdEpic() {
        manager.addNewEpic(new Epic("Эпик 1", "Первый эпик", Status.NEW));
        Epic epic1 = manager.getEpicById(1);
        Epic epic2 = manager.getEpicById(1);
        Assertions.assertEquals(epic1, epic2);
    }

    @Test
    public void shouldReturnTrueByEqualIdSubtask() {
        manager.addNewEpic(new Epic("Эпик", "", Status.NEW));
        manager.addNewSubtask(new Subtask("Подзадача", "", Status.NEW, 1));
        Subtask subtask1 = manager.getSubtaskById(2);
        Subtask subtask2 = manager.getSubtaskById(2);
        Assertions.assertEquals(subtask1, subtask2);
    }

    @Test
    void checkHistoryManagerSavesTaskVersions() {
        Task checkTask = new Task("Задача 1", "Первая задача", Status.NEW);
        manager.addNewTask(checkTask);
        manager.getTaskById(1);
        Task checkTask2 = new Task(checkTask.getName(), checkTask.getDescription(), checkTask.getStatus());
        checkTask2.setId(checkTask.getId());
        checkTask2.setType(checkTask.getType());
        checkTask.setStatus(Status.DONE);
        assertEquals(checkTask2, manager.getHistory().getFirst());

    }
}