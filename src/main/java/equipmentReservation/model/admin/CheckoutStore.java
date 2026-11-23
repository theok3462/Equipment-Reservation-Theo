package equipmentReservation.model.admin;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;

public interface CheckoutStore {
    Reservation findReservation(String id) throws Exception;

    boolean hasOpenMaintenance(String equipmentId) throws Exception;

    boolean hasOpenCheckout(String equipmentId) throws Exception;

    CheckoutRecord findCheckout(String reservationId) throws Exception;

    List<CheckoutRecord> listOpen() throws Exception;

    void expireDueReservations(LocalDateTime now, int pickupWindowMinutes) throws Exception;

    void checkOut(String reservationId, String admin, LocalDateTime time) throws Exception;

    void returnItem(String reservationId, Condition condition, String admin, LocalDateTime time)
            throws Exception;
}
