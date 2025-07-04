import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node> requestHistory = new HashMap<>();
    private Node head;
    private Node tail;

    private void linkLast(Task task) {
        final Node newNode = new Node(task);
        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        requestHistory.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node != null) {
            if (node == head) { //delete head
                if (head.next == null) { //if only one element
                    head = null;
                    tail = null;
                } else {
                    head = head.next;
                    head.prev = null;
                }
            } else if (node == tail) { //delete tail
                tail = tail.prev;
                tail.next = null;
            } else { //delete middle node
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = requestHistory.remove(id);
        removeNode(nodeToRemove);
    }

    @Override
    public List<Task> getHistory() {
        //Реализация метода getHistory должна перекладывать задачи
        //из связного списка в ArrayList для формирования ответа.
        final ArrayList<Task> history = new ArrayList<>();
        Node currentNode = tail;
        while (currentNode != null) {
            history.add(currentNode.task);
            currentNode = currentNode.prev;
        }
        return history;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        public Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }
}


