package equipmentReservation.model;

/** Thrown when a required field or other input is invalid. */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
