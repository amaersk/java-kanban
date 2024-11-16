import task.Task;

import java.util.ArrayList;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {

    private final static int HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        if (history.size() == HISTORY_SIZE) {
            history.removeFirst();
        }

        Task task1 = new Task(task.getName(), task.getDescription(), task.getStatus());
        task1.setId(task.getId());
        task1.setType(task.getType());
        history.add(task1);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(history);
    }
}


