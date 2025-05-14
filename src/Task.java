

public class Task {
    public int id;
    public String nameTask;
    public String description;
    StatusTask statusTask;

    public Task(int id, String nameTask, String description, StatusTask statusTask) {
        this.id = id;
        this.nameTask = nameTask;
        this.description = description;
        this.statusTask = statusTask;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public StatusTask getStatusTask() {
        return statusTask;
    }

    public void setStatusTask(StatusTask statusTask) {
        this.statusTask = statusTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;

        if (getId() != task.getId()) return false;
        if (!getNameTask().equals(task.getNameTask())) return false;
        if (!getDescription().equals(task.getDescription())) return false;
        return getStatusTask() == task.getStatusTask();
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getNameTask().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getStatusTask().hashCode();
        return result;
    }
}
