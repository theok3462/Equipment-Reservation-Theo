package equipmentReservation.model.admin;

import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.InvalidOperationException;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import java.awt.event.ActionEvent;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class MaintenanceService extends AbstractModel {
    private final MaintenanceStore store;
    private final Supplier<User> currentUser;
    private final Clock clock;

    public MaintenanceService(MaintenanceStore store, Supplier<User> currentUser, Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.currentUser = Objects.requireNonNull(currentUser);
        this.clock = Objects.requireNonNull(clock);
    }

    public MaintenanceRecord openMaintenance(String equipmentId, String reason) throws Exception {
        User admin = requireAdmin();
        String id = required(equipmentId, "equipmentId", "Equipment ID");
        String why = required(reason, "reason", "Reason");
        if (store.findEquipment(id) == null) {
            throw new NotFoundException("Equipment " + id + " was not found.");
        }
        if (store.hasOpenCheckout(id)) {
            throw new InvalidOperationException(id
                    + " is checked out; record the return first.");
        }
        if (store.hasOpenMaintenance(id)) {
            throw new InvalidOperationException(id + " is already under maintenance.");
        }
        MaintenanceRecord record = store.open(id, why, admin.getUsername(),
                LocalDateTime.now(clock));
        changed();
        return record;
    }

    public MaintenanceRecord closeMaintenance(String recordId) throws Exception {
        User admin = requireAdmin();
        String id = required(recordId, "recordId", "Maintenance record ID");
        MaintenanceRecord record = store.findRecord(id);
        if (record == null) {
            throw new NotFoundException("Maintenance record " + id + " was not found.");
        }
        if (!record.isOpen()) {
            throw new InvalidOperationException("Maintenance record is already closed.");
        }
        MaintenanceRecord closed = store.close(id, admin.getUsername(), LocalDateTime.now(clock));
        changed();
        return closed;
    }

    public List<MaintenanceRecord> listOpen() throws Exception {
        requireAdmin();
        return List.copyOf(store.listOpen());
    }

    public List<MaintenanceRecord> listFor(String equipmentId) throws Exception {
        requireAdmin();
        return List.copyOf(store.listFor(required(equipmentId, "equipmentId", "Equipment ID")));
    }

    private User requireAdmin() throws AuthorizationException {
        User user = currentUser.get();
        if (user == null || !user.isAdmin()) {
            throw new AuthorizationException("Administrator access is required.");
        }
        return user;
    }

    private String required(String text, String field, String label) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException(field, label + " is required.");
        }
        return text.trim();
    }

    private void changed() {
        notifyChanged(new ModelEvent(this, ActionEvent.ACTION_PERFORMED, "MAINTENANCE"));
        notifyChanged(new ModelEvent(this, ActionEvent.ACTION_PERFORMED, "EQUIPMENT"));
    }
}
