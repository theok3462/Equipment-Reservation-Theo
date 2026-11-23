package equipmentReservation.model.admin;

import equipmentReservation.model.Condition;
import java.time.LocalDateTime;

public record CheckoutRecord(String reservationId, String borrower, String equipmentId,
        LocalDateTime scheduledEnd, LocalDateTime checkedOutAt, String checkedOutBy,
        LocalDateTime returnedAt, String returnedBy, Condition returnCondition) {
    public boolean isOpen() {
        return returnedAt == null;
    }
}
