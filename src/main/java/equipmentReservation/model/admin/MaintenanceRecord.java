package equipmentReservation.model.admin;

import java.time.LocalDateTime;

public record MaintenanceRecord(String id, String equipmentId, String reason,
        LocalDateTime openedAt, String openedBy,
        LocalDateTime closedAt, String closedBy) {
    public boolean isOpen() {
        return closedAt == null;
    }
}
