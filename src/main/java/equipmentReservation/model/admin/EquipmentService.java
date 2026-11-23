package equipmentReservation.model.admin;

import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import java.awt.event.ActionEvent;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class EquipmentService extends AbstractModel {
    private final EquipmentAdminStore store;
    private final Supplier<User> currentUser;
    private final Clock clock;

    public EquipmentService(EquipmentAdminStore store, Supplier<User> currentUser, Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.currentUser = Objects.requireNonNull(currentUser);
        this.clock = Objects.requireNonNull(clock);
    }

    private User requireSignIn() throws AuthorizationException {
        User user = currentUser.get();
        if (user == null) {
            throw new AuthorizationException("Sign in is required.");
        }
        return user;
    }

    private User requireAdmin() throws AuthorizationException {
        User user = requireSignIn();
        if (!user.isAdmin()) {
            throw new AuthorizationException("Administrator access is required.");
        }
        return user;
    }

    private String requireId(String id) throws ValidationException {
        if (id == null || id.isBlank()) {
            throw new ValidationException("equipmentId", "Equipment ID is required.");
        }
        return id.trim();
    }

    private Equipment checkedEquipment(Equipment equipment) throws ValidationException {
        if (equipment == null) {
            throw new ValidationException("equipment", "Equipment is required.");
        }
        return new Equipment(equipment.getId(), equipment.getName(), equipment.getCategory(),
                equipment.getDescription(), equipment.getCondition(), equipment.getOperationalStatus());
    }

    public void addEquipment(Equipment equipment) throws Exception {
        requireAdmin();
        store.insert(checkedEquipment(equipment));
        equipmentChanged();
    }

    public void updateEquipment(Equipment equipment) throws Exception {
        requireAdmin();
        store.update(checkedEquipment(equipment));
        equipmentChanged();
    }

    public Equipment getEquipment(String id) throws Exception {
        requireSignIn();
        Equipment equipment = store.find(requireId(id));
        if (equipment == null) {
            throw new NotFoundException("Equipment " + id + " was not found.");
        }
        return equipment;
    }

    public List<Equipment> listEquipment() throws Exception {
        requireSignIn();
        return List.copyOf(store.findAll());
    }

    public List<String> listCategories() throws Exception {
        requireSignIn();
        return store.findAll().stream()
                .map(Equipment::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    public Equipment returnToService(String id, Condition condition) throws Exception {
        User admin = requireAdmin();
        String equipmentId = requireId(id);
        if (condition == null || condition == Condition.DAMAGED) {
            throw new ValidationException("condition",
                    "Choose Good or Fair to return an item to service.");
        }
        Equipment updated = store.returnToService(equipmentId, condition,
                admin.getUsername(), clock.instant());
        equipmentChanged();
        return updated;
    }

    private void equipmentChanged() {
        notifyChanged(new ModelEvent(this, ActionEvent.ACTION_PERFORMED, "EQUIPMENT"));
    }
}
