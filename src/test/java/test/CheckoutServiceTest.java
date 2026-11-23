package test;

import equipmentReservation.model.*;
import equipmentReservation.model.admin.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutServiceTest {
    private final LocalDateTime start = LocalDateTime.of(2026, 11, 23, 9, 0);
    private final User admin = user("admin", Role.ADMIN);
    private final User borrower = user("borrower", Role.BORROWER);
    private AtomicReference<User> currentUser;
    private FakeStore store;
    private FakeSettings settings;

    private static User user(String name, Role role) {
        try {
            return new User(name, "hash", "salt", role);
        } catch (ValidationException e) {
            throw new AssertionError(e);
        }
    }

    private static Equipment item() {
        try {
            return new Equipment("EQ-1", "Camera", "Camera", "",
                    Condition.GOOD, OperationalStatus.OPERATIONAL);
        } catch (ValidationException e) {
            throw new AssertionError(e);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        currentUser = new AtomicReference<>(admin);
        store = new FakeStore();
        store.reservation = new Reservation("R-1", borrower, item(), start,
                start.plusHours(2), start.minusDays(1));
        settings = new FakeSettings();
    }

    private CheckoutService service(int minute) {
        Clock clock = Clock.fixed(start.plusMinutes(minute).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC);
        return new CheckoutService(store, new SettingsService(settings, currentUser::get),
                currentUser::get, clock);
    }

    @Test
    void checkoutAtStartIsAllowed() throws Exception {
        assertNotNull(service(0).checkOut("R-1"));
        assertNotNull(store.record);
    }

    @Test
    void checkoutBeforeDeadlineIsAllowed() throws Exception {
        assertNotNull(service(29).checkOut("R-1"));
    }

    @Test
    void checkoutAtDeadlineIsRejected() {
        assertThrows(InvalidOperationException.class, () -> service(30).checkOut("R-1"));
    }

    @Test
    void checkoutBeforeStartIsRejected() {
        assertThrows(InvalidOperationException.class, () -> service(-1).checkOut("R-1"));
    }

    @Test
    void borrowerCannotCheckOutOrReturn() throws Exception {
        currentUser.set(borrower);
        assertThrows(AuthorizationException.class, () -> service(0).checkOut("R-1"));
        assertThrows(AuthorizationException.class,
                () -> service(0).returnItem("R-1", Condition.GOOD));
    }

    @Test
    void maintenanceBlocksCheckout() {
        store.maintenance = true;
        assertThrows(InvalidOperationException.class, () -> service(0).checkOut("R-1"));
    }

    @Test
    void returnDamagedAndRejectSecondReturn() throws Exception {
        service(0).checkOut("R-1");
        CheckoutRecord returned = service(45).returnItem("R-1", Condition.DAMAGED);
        assertEquals(Condition.DAMAGED, returned.returnCondition());
        assertFalse(returned.isOpen());
        assertThrows(InvalidOperationException.class,
                () -> service(46).returnItem("R-1", Condition.GOOD));
    }

    @Test
    void overdueAppearsOnlyWhileOpen() throws Exception {
        service(0).checkOut("R-1");
        assertEquals(1, service(180).listOverdue().size());
        assertEquals(Duration.ofHours(1), service(180).listOverdue().get(0).overdueBy());
        service(180).returnItem("R-1", Condition.GOOD);
        assertTrue(service(181).listOverdue().isEmpty());
    }

    private static class FakeSettings implements SettingsStore {
        private Integer minutes;
        public Integer getPickupWindowMinutes() { return minutes; }
        public void setPickupWindowMinutes(int value) { minutes = value; }
    }

    private class FakeStore implements CheckoutStore {
        Reservation reservation;
        CheckoutRecord record;
        boolean maintenance;

        public Reservation findReservation(String id) {
            return "R-1".equals(id) ? reservation : null;
        }
        public List<Reservation> searchReservations(String query, ReservationStatus status) {
            return reservation.getStatus() == status ? List.of(reservation) : List.of();
        }
        public boolean hasOpenMaintenance(String id) { return maintenance; }
        public boolean hasOpenCheckout(String id) {
            return record != null && record.isOpen();
        }
        public CheckoutRecord findCheckout(String id) {
            return record != null && record.reservationId().equals(id) ? record : null;
        }
        public List<CheckoutRecord> listOpen() {
            return record != null && record.isOpen() ? List.of(record) : List.of();
        }
        public void expireDueReservations(LocalDateTime now, int window) {}
        public void checkOut(String id, String admin, LocalDateTime now) {
            record = new CheckoutRecord(id, borrower.getUsername(), "EQ-1",
                    reservation.getEnd(), now, admin, null, null, null);
        }
        public void returnItem(String id, Condition condition, String admin, LocalDateTime now) {
            record = new CheckoutRecord(id, record.borrower(), record.equipmentId(),
                    record.scheduledEnd(), record.checkedOutAt(), record.checkedOutBy(),
                    now, admin, condition);
        }
    }
}
