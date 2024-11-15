import task.Task;

import java.util.ArrayList;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {

    private final static int HISTORY_SIZE = 10;
    private final List<Task> listHistory = new ArrayList<>();

    @Override
    public void add(Task task) {

        if (listHistory.size() == HISTORY_SIZE) {
            listHistory.removeFirst();
        }
        if (task != null) {
            Task task1 = new Task(task.getName(), task.getDescription(), task.getStatus());
            task1.setId(task.getId());
            task1.setType(task.getType());
            listHistory.add(task1);
        }
    }

    @Override
    public List<Task> getHistory() {
        return listHistory;
    }
}

