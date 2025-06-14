package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> idSubtaskArray = new ArrayList<>(); // список ID подзадач
    private LocalDateTime endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
        this.setType(Type.EPIC);
    }

    public void addIdSubtask(int idSubtask) {
        idSubtaskArray.add(idSubtask);
    }

    public ArrayList<Integer> getIdSubtaskArray() {
        return idSubtaskArray;
    }

    public void setIdSubtaskArray(ArrayList<Integer> idSubtaskArray) {
        this.idSubtaskArray = idSubtaskArray;
    }

    public void clearSubtaskArray() {
        idSubtaskArray.clear();
    }

    public void deleteEpicSubtask(Integer idSubtask) {
        idSubtaskArray.remove(idSubtask);
    }

    @Override
    public Duration getDuration() {
        return null; // Продолжительность рассчитывается на основе подзадач
    }

    @Override
    public void setDuration(Duration duration) {
        // Продолжительность рассчитывается на основе подзадач, поэтому этот метод ничего не делает
    }

    @Override
    public LocalDateTime getStartTime() {
        return null; // Время начала рассчитывается на основе подзадач
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        // Время начала рассчитывается на основе подзадач, поэтому этот метод ничего не делает
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(idSubtaskArray, epic.idSubtaskArray) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idSubtaskArray, endTime);
    }
}
