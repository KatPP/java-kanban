package tracker.entity;

import tracker.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Эпик - задача, состоящая из подзадач.
 * Время начала, окончания и продолжительность рассчитываются на основе подзадач.
 */
public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    /**
     * Конструктор эпика.
     *
     * @param id          уникальный идентификатор эпика
     * @param name        название эпика
     * @param description описание эпика
     */
    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW, Duration.ZERO, null);
    }

    /**
     * @return список идентификаторов подзадач, относящихся к эпику
     */
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    /**
     * Добавляет подзадачу к эпику.
     *
     * @param subtaskId идентификатор подзадачи
     */
    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    /**
     * Удаляет подзадачу из эпика.
     *
     * @param subtaskId идентификатор подзадачи
     */
    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    /**
     * Пересчитывает временные параметры эпика на основе подзадач.
     *
     * @param taskManager менеджер задач для доступа к подзадачам
     */
    public void updateTime(TaskManager taskManager) {
        if (subtaskIds.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = taskManager.getSubtask(subtaskId);
            if (subtask == null) continue;

            Duration subtaskDuration = subtask.getDuration();
            LocalDateTime subtaskStart = subtask.getStartTime();
            LocalDateTime subtaskEnd = subtask.getEndTime();

            if (subtaskStart != null) {
                if (earliestStart == null || subtaskStart.isBefore(earliestStart)) {
                    earliestStart = subtaskStart;
                }
            }

            if (subtaskEnd != null) {
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
            }

            if (subtaskDuration != null) {
                totalDuration = totalDuration.plus(subtaskDuration);
            }
        }

        setDuration(totalDuration);
        setStartTime(earliestStart);
    }
}