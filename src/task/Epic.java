package task;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> idSubtaskArray = new ArrayList<>(); //список ID подзадач

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(idSubtaskArray, epic.idSubtaskArray);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idSubtaskArray);
    }

}
