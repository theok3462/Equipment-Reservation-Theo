package equipmentReservation.controller;

import equipmentReservation.model.Reservation;
import equipmentReservation.model.admin.MaintenanceRecord;
import equipmentReservation.model.admin.MaintenanceService;
import equipmentReservation.view.MaintenanceView;
import java.util.List;

public class MaintenanceController extends AbstractController {
    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
        setModel(service);
    }

    public List<Reservation> activeReservationsFor(String equipmentId) throws Exception {
        return service.getActiveReservationsFor(equipmentId);
    }

    public MaintenanceRecord open(String equipmentId, String reason) throws Exception {
        return service.openMaintenance(equipmentId, reason);
    }

    public MaintenanceRecord close(String recordId) throws Exception {
        return service.closeMaintenance(recordId);
    }

    @Override
    public void operation(String option) {
        if ("refresh".equals(option) && getView() instanceof MaintenanceView view) {
            view.refreshMaintenance();
        }
    }
}
