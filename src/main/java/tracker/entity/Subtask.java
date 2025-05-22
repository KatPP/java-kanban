package tracker.entity;

public class Subtask extends Task {
    private final int epicId; // ID эпика, к которому относится подзадача

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}
