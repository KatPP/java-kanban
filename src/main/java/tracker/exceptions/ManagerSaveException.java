package tracker.exceptions;

/**
 * Исключение, возникающее при ошибках сохранения или загрузки данных менеджера задач в файл.
 * Наследуется от {@link RuntimeException}, чтобы не требовать обязательной обработки в коде.
 */
public class ManagerSaveException extends RuntimeException {
    /**
     * Создает исключение с сообщением об ошибке и причиной.
     *
     * @param message описание ошибки.
     */
    public ManagerSaveException(String message) {
        super(message);
    }
}