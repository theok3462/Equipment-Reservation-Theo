package equipmentReservation.model.admin;

import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.Condition;
import equipmentReservation.model.InvalidOperationException;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import java.awt.event.ActionEvent;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CheckoutService extends AbstractModel {
    private final CheckoutStore store;
    private final SettingsService settings;
    private final Supplier<User> currentUser;
    private final Clock clock;

    public record Overdue(CheckoutRecord checkout, Duration overdueBy) {}

    public CheckoutService(CheckoutStore store, SettingsService settings,
            Supplier<User> currentUser, Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.settings = Objects.requireNonNull(settings);
        this.currentUser = Objects.requireNonNull(currentUser);
        this.clock = Objects.requireNonNull(clock);
    }

    public CheckoutRecord checkOut(String reservationId) throws Exception {
        User admin = requireAdmin();
        String id = requireId(reservationId);
        LocalDateTime now = LocalDateTime.now(clock);
        int window = settings.getPickupWindowMinutes();
        store.expireDueReservations(now, window);
        Reservation reservation = store.findReservation(id);
        if (reservation == null) {
            throw new NotFoundException("Reservation " + id + " was not found.");
        }
        if (reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new InvalidOperationException("Reservation " + id
                    + " has expired and cannot be checked out.");
        }
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidOperationException("Reservation " + id + " is not active.");
        }
        if (now.isBefore(reservation.getStart())) {
            throw new InvalidOperationException("Too early: pickup starts at "
                    + reservation.getStart() + ".");
        }
        LocalDateTime deadline = reservation.getStart().plusMinutes(window);
        if (reservation.getEnd().isBefore(deadline)) {
            deadline = reservation.getEnd();
        }
        if (!now.isBefore(deadline)) {
            throw new InvalidOperationException("Reservation " + id
                    + " has expired and cannot be checked out.");
        }
        String equipmentId = reservation.getItem().getId();
        if (reservation.getItem().getOperationalStatus() != OperationalStatus.OPERATIONAL
                || store.hasOpenMaintenance(equipmentId)
                || store.hasOpenCheckout(equipmentId)) {
            throw new InvalidOperationException("Equipment is not available for checkout.");
        }
        store.checkOut(id, admin.getUsername(), now);
        notifyEvent("CHECKOUTS");
        notifyEvent("RESERVATIONS");
        return store.findCheckout(id);
    }

    public CheckoutRecord returnItem(String reservationId, Condition condition) throws Exception {
        User admin = requireAdmin();
        String id = requireId(reservationId);
        if (condition == null) {
            throw new ValidationException("condition", "Return condition is required.");
        }
        CheckoutRecord checkout = store.findCheckout(id);
        if (checkout == null || !checkout.isOpen()) {
            throw new InvalidOperationException(id
                    + " has no open checkout (already returned?).");
        }
        store.returnItem(id, condition, admin.getUsername(), LocalDateTime.now(clock));
        notifyEvent("CHECKOUTS");
        notifyEvent("RESERVATIONS");
        notifyEvent("EQUIPMENT");
        return store.findCheckout(id);
    }

    public List<Reservation> searchReservations(String query, ReservationStatus status) throws Exception {
        requireAdmin();
        return List.copyOf(store.searchReservations(query == null ? "" : query.trim(), status));
    }

    public LocalDateTime getPickupDeadline(Reservation reservation) throws Exception {
        LocalDateTime deadline = reservation.getStart().plusMinutes(settings.getPickupWindowMinutes());
        return reservation.getEnd().isBefore(deadline) ? reservation.getEnd() : deadline;
    }

    public List<CheckoutRecord> listOpenCheckouts() throws Exception {
        requireAdmin();
        return List.copyOf(store.listOpen());
    }

    public List<Overdue> listOverdue() throws Exception {
        requireAdmin();
        LocalDateTime now = LocalDateTime.now(clock);
        return store.listOpen().stream()
                .filter(c -> c.scheduledEnd().isBefore(now))
                .map(c -> new Overdue(c, Duration.between(c.scheduledEnd(), now)))
                .toList();
    }

    private String requireId(String id) throws ValidationException {
        if (id == null || id.isBlank()) {
            throw new ValidationException("reservationId", "Reservation ID is required.");
        }
        return id.trim();
    }

    private User requireAdmin() throws AuthorizationException {
        User user = currentUser.get();
        if (user == null || !user.isAdmin()) {
            throw new AuthorizationException("Administrator access is required.");
        }
        return user;
    }

    private void notifyEvent(String command) {
        notifyChanged(new ModelEvent(this, ActionEvent.ACTION_PERFORMED, command));
    }
}
