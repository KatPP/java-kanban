package tracker.service;

import tracker.entity.*;
import tracker.exceptions.ManagerSaveException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация менеджера задач, хранящая данные в оперативной памяти.
 * Поддерживает приоритизацию задач, проверку пересечений по времени и управление историей просмотров.
 */
public class InMemoryTaskManager implements TaskManager {
    int nextId = 1;
    final Map<Integer, Task> tasks = new HashMap<>();
    final Map<Integer, Subtask> subtasks = new HashMap<>();
    final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()))
    );

    // Генерация нового ID
    private int generateId() {
        return nextId++;
    }

    // История
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Методы для Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(String name, String description, Status status) {
        return null;
    }

    @Override
    public Task createTask(String name, String description, Status status,
                           Duration duration, LocalDateTime startTime) {
        Task newTask = new Task(0, name, description, status, duration, startTime);
        if (hasTimeConflict(newTask)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей");
        }

        int id = generateId();
        Task task = new Task(id, name, description, status, duration, startTime);
        tasks.put(id, task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            if (hasTimeConflict(task) && !task.equals(existingTask)) {
                throw new ManagerSaveException("Обновленная задача пересекается по времени с существующей");
            }

            removeFromPrioritized(existingTask);
            tasks.put(task.getId(), task);
            addToPrioritized(task);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            removeFromPrioritized(task);
        }
    }

    // Методы для Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            epic.updateTime(this);
        });
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Subtask createSubtask(String name, String description, Status status, int epicId) {
        return null;
    }

    @Override
    public Subtask createSubtask(String name, String description, Status status,
                                 int epicId, Duration duration, LocalDateTime startTime) {
        Subtask newSubtask = new Subtask(0, name, description, status, epicId, duration, startTime);
        if (hasTimeConflict(newSubtask)) {
            throw new ManagerSaveException("Подзадача пересекается по времени с существующей");
        }

        int id = generateId();
        Subtask subtask = new Subtask(id, name, description, status, epicId, duration, startTime);
        subtasks.put(id, subtask);

        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtask(id);
            updateEpicStatus(epicId);
            epic.updateTime(this);
        }

        addToPrioritized(subtask);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            if (hasTimeConflict(subtask) && !subtask.equals(existingSubtask)) {
                throw new ManagerSaveException("Обновленная подзадача пересекается по времени с существующей");
            }

            removeFromPrioritized(existingSubtask);
            subtasks.put(subtask.getId(), subtask);
            addToPrioritized(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic.getId());
                epic.updateTime(this);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            removeFromPrioritized(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
                epic.updateTime(this);
            }
        }
    }

    // Методы для Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().forEach(subtaskId -> {
                historyManager.remove(subtaskId);
                removeFromPrioritized(subtasks.get(subtaskId));
            });
        });

        subtasks.values().forEach(this::removeFromPrioritized);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic createEpic(String name, String description) {
        int id = generateId();
        Epic epic = new Epic(id, name, description);
        epics.put(id, epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic savedEpic = epics.get(epic.getId());
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
            savedEpic.updateTime(this);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
                removeFromPrioritized(subtasks.get(subtaskId));
            });
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Приоритизация задач
    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .collect(Collectors.toList());
    }

    // Проверка пересечений по времени
    @Override
    public boolean hasTimeConflict(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && !task.equals(newTask))
                .anyMatch(existingTask -> {
                    LocalDateTime existingStart = existingTask.getStartTime();
                    LocalDateTime existingEnd = existingTask.getEndTime();
                    return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
                });
    }

    // Вспомогательные методы для работы с приоритетами
    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    // Обновление статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}