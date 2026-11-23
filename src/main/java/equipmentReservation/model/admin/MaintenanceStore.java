package equipmentReservation.model.admin;

import equipmentReservation.model.Equipment;
import equipmentReservation.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceStore {
    Equipment findEquipment(String equipmentId) throws Exception;

    boolean hasOpenCheckout(String equipmentId) throws Exception;

    List<Reservation> getActiveReservationsFor(String equipmentId) throws Exception;

    boolean hasOpenMaintenance(String equipmentId) throws Exception;

    MaintenanceRecord findRecord(String recordId) throws Exception;

    MaintenanceRecord open(String equipmentId, String reason, String admin,
            LocalDateTime time) throws Exception;

    MaintenanceRecord close(String recordId, String admin, LocalDateTime time) throws Exception;

    List<MaintenanceRecord> listOpen() throws Exception;

    List<MaintenanceRecord> listFor(String equipmentId) throws Exception;
}
