package task;

import java.util.ArrayList;

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

}
