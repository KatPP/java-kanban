import java.util.Objects;

public class Task {
    private final int id;  // сделал поле final
    private String nameTask;
    private String description;
    private Status statusTask;

    public Task(int id, String nameTask, String description, Status statusTask) {
        this.id = id;
        this.nameTask = nameTask;
        this.description = description;
        this.statusTask = statusTask;
    }

    public int getId() {
        return id;
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatusTask() {
        return statusTask;
    }

    public void setStatusTask(Status statusTask) {
        this.statusTask = statusTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;  // сравниваем только по id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);  // хэш-код только по id
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", nameTask='" + nameTask + '\'' +
                ", description='" + description + '\'' +
                ", statusTask=" + statusTask +
                '}';
    }
}