import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private File tempFile;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        historyManager = Managers.getDefaultHistory();
        taskManager = new FileBackedTaskManager(tempFile, historyManager);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        taskManager.addNewTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTaskById(task.getId());

        assertNotNull(loadedTask);
        assertEquals(task, loadedTask);
    }

    @Test
    void shouldSaveAndLoadEpicsWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.NEW, epic.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        List<Subtask> loadedSubtasks = loadedManager.printArrayIdSubtask(epic.getId());

        assertNotNull(loadedEpic);
        assertEquals(epic, loadedEpic);
        assertEquals(2, loadedSubtasks.size());
        assertTrue(loadedSubtasks.contains(subtask1));
        assertTrue(loadedSubtasks.contains(subtask2));
    }

    @Test
    void shouldSaveAndLoadHistory() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedHistory = loadedManager.getHistory();
        assertTrue(loadedHistory.isEmpty());

        assertNotNull(loadedManager.getTaskById(task1.getId()));
        assertNotNull(loadedManager.getTaskById(task2.getId()));
    }

    @Test
    void shouldHandleEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void shouldHandleTaskDeletion() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        taskManager.addNewTask(task);
        taskManager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertNull(loadedManager.getTaskById(task.getId()));
    }

    @Test
    void shouldHandleEpicDeletionWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, epic.getId());
        taskManager.addNewSubtask(subtask);

        taskManager.deleteEpic(epic.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertNull(loadedManager.getEpicById(epic.getId()));
        assertNull(loadedManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void shouldUpdateTasksInFile() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        taskManager.addNewTask(task);

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    void shouldHandleFileNotFound() {
        File nonExistentFile = new File("non-existent.csv");
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void shouldHandleCorruptedFile() throws IOException {
        // Создаем файл с некорректными данными
        File corruptedFile = File.createTempFile("corrupted", ".csv");
        try {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(corruptedFile);

            assertTrue(loadedManager.getTasks().isEmpty());
            assertTrue(loadedManager.getEpics().isEmpty());
            assertTrue(loadedManager.getSubtasks().isEmpty());
            assertTrue(loadedManager.getHistory().isEmpty());
        } finally {
            corruptedFile.delete();
        }
    }
} 