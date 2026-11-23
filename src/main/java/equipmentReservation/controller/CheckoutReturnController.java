package equipmentReservation.controller;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.admin.CheckoutRecord;
import equipmentReservation.model.admin.CheckoutService;
import equipmentReservation.view.CheckoutReturnView;
import java.util.List;

public class CheckoutReturnController extends AbstractController {
    private final CheckoutService service;

    public CheckoutReturnController(CheckoutService service) {
        this.service = service;
        setModel(service);
    }

    public List<Reservation> search(String query, ReservationStatus status) throws Exception {
        return service.searchReservations(query, status);
    }

    public CheckoutRecord checkOut(String reservationId) throws Exception {
        return service.checkOut(reservationId);
    }

    public CheckoutRecord recordReturn(String reservationId, Condition condition) throws Exception {
        return service.returnItem(reservationId, condition);
    }

    @Override
    public void operation(String option) {
        if ("refresh".equals(option) && getView() instanceof CheckoutReturnView view) {
            view.refreshAll();
        }
    }
}
