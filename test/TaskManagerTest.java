import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault(Managers.getDefaultHistory());
    }

    @Test
    void shouldAddNewTask() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        manager.addNewTask(task);

        Task savedTask = manager.getTaskById(task.getId());
        assertNotNull(savedTask);
        assertEquals(task, savedTask);
    }

    @Test
    void shouldAddNewEpic() {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        manager.addNewEpic(epic);

        Epic savedEpic = manager.getEpicById(epic.getId());
        assertNotNull(savedEpic);
        assertEquals(epic, savedEpic);
    }

    @Test
    void shouldUpdateTaskStatus() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        manager.addNewTask(task);

        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);

        Task updatedTask = manager.getTaskById(task.getId());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.NEW, epic.getId());
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus());
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        manager.addNewTask(task);

        manager.deleteTask(task.getId());
        assertNull(manager.getTaskById(task.getId()));
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        manager.deleteEpic(epic.getId());
        assertNull(manager.getEpicById(epic.getId()));
        assertNull(manager.getSubtaskById(subtask.getId()));
    }


    @Test
    public void shouldReturnTrueByEqualIdTask() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistory());
        manager.addNewTask(new Task("Задача 1", "Первая задача", Status.NEW));
        Task task1 = manager.getTaskById(1);
        Task task2 = manager.getTaskById(1);
        assertEquals(task1, task2);
    }

    @Test
    public void shouldReturnTrueByEqualIdEpic() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistory());
        manager.addNewEpic(new Epic("Эпик 1", "Первый эпик", Status.NEW));
        Epic epic1 = manager.getEpicById(1);
        Epic epic2 = manager.getEpicById(1);
        assertEquals(epic1, epic2);
    }

    @Test
    public void shouldReturnTrueByEqualIdSubtask() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistory());
        manager.addNewEpic(new Epic("Эпик", "", Status.NEW));
        manager.addNewSubtask(new Subtask("Подзадача", "", Status.NEW, 1));
        Subtask subtask1 = manager.getSubtaskById(2);
        Subtask subtask2 = manager.getSubtaskById(2);
        assertEquals(subtask1, subtask2);
    }

    @Test
    void checkHistoryManagerSavesTaskVersions() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistory());
        Task checkTask = new Task("Задача 1", "Первая задача", Status.NEW);
        manager.addNewTask(checkTask);
        manager.getTaskById(checkTask.getId());
        Task checkTask2 = new Task(checkTask.getName(), checkTask.getDescription(), checkTask.getStatus());
        checkTask2.setId(checkTask.getId());
        checkTask2.setType(checkTask.getType());
        manager.updateTask(checkTask2);
        checkTask.setStatus(Status.DONE);
        assertEquals(checkTask, manager.getHistory().getFirst());
    }
}