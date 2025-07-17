package tracker.entity;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Подзадача, относящаяся к определенному эпику.
 * Содержит ссылку на эпик через epicId.
 */
public class Subtask extends Task {
    private final int epicId;

    /**
     * Конструктор подзадачи.
     *
     * @param id          уникальный идентификатор подзадачи
     * @param name        название подзадачи
     * @param description описание подзадачи
     * @param status      текущий статус подзадачи
     * @param epicId      идентификатор эпика, к которому относится подзадача
     * @param duration    продолжительность выполнения подзадачи
     * @param startTime   дата и время начала выполнения подзадачи
     */
    public Subtask(int id, String name, String description, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    /**
     * @return идентификатор эпика, к которому относится подзадача
     */
    public int getEpicId() {
        return epicId;
    }
}