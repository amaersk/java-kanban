import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TaskManagerTest {





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