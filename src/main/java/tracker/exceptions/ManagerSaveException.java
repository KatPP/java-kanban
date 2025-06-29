package tracker.exceptions;

import java.io.IOException;

/**
 * Исключение, возникающее при ошибках сохранения или загрузки данных менеджера задач в файл.
 * Наследуется от {@link RuntimeException}, чтобы не требовать обязательной обработки в коде.
 */
public class ManagerSaveException extends RuntimeException {
    /**
     * Создает исключение с сообщением об ошибке и причиной.
     *
     * @param message описание ошибки.
     * @param cause   исключение, вызвавшее эту ошибку (например, {@link IOException}).
     */
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}