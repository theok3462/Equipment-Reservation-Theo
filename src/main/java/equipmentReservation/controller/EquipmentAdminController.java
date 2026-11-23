package equipmentReservation.controller;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.admin.EquipmentService;
import equipmentReservation.view.EquipmentManagementView;

public class EquipmentAdminController extends AbstractController {
    private final EquipmentService service;

    public EquipmentAdminController(EquipmentService service) {
        this.service = service;
        setModel(service);
    }

    public void save(boolean isNew, String id, String name, String category,
            String description, Condition condition, OperationalStatus status) throws Exception {
        Equipment equipment = new Equipment(id, name, category, description, condition, status);
        if (isNew) {
            service.addEquipment(equipment);
        } else {
            service.updateEquipment(equipment);
        }
    }

    public void returnToService(String equipmentId, Condition condition) throws Exception {
        service.returnToService(equipmentId, condition);
    }

    @Override
    public void operation(String option) {
        if (getView() instanceof EquipmentManagementView view) {
            if ("refresh".equals(option)) {
                view.refreshEquipment();
            } else if ("new".equals(option)) {
                view.newItem();
            }
        }
    }
}
