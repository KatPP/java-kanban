package tracker.service;

import tracker.entity.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id);  // Новый метод для удаления
    List<Task> getHistory();
}
