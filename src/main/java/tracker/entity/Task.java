package tracker.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Базовый класс для представления задачи.
 * Содержит информацию о названии, описании, статусе, времени начала, продолжительности и времени окончания.
 */
public class Task {
    private final int id;
    private String name;
    private String description;
    private Status status;
    private Duration duration;
    private LocalDateTime startTime;

    /**
     * Конструктор задачи.
     *
     * @param id          уникальный идентификатор задачи
     * @param name        название задачи
     * @param description описание задачи
     * @param status      текущий статус задачи
     * @param duration    продолжительность выполнения задачи в минутах
     * @param startTime   дата и время начала выполнения задачи
     */
    public Task(int id, String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    /**
     * Возвращает время окончания задачи.
     * Рассчитывается как startTime + duration.
     *
     * @return время окончания задачи или null, если не задано время начала
     */
    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    /**
     * @return уникальный идентификатор задачи
     */
    public int getId() {
        return id;
    }

    /**
     * @return название задачи
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название задачи.
     *
     * @param name новое название задачи
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return описание задачи
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание задачи.
     *
     * @param description новое описание задачи
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return текущий статус задачи
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Устанавливает статус задачи.
     *
     * @param status новый статус задачи
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return продолжительность выполнения задачи
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Устанавливает продолжительность выполнения задачи.
     *
     * @param duration новая продолжительность
     */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * @return дата и время начала выполнения задачи
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Устанавливает дату и время начала выполнения задачи.
     *
     * @param startTime новое время начала
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // Остальные методы (equals, hashCode, toString) остаются без изменений
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}