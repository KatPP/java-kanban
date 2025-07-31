package tracker.exceptions;

/**
 * Исключение при попытке доступа к несуществующей задаче.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
