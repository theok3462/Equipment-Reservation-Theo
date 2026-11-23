package test;

import equipmentReservation.model.*;
import equipmentReservation.model.admin.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceServiceTest {
    private final User admin = user("admin", Role.ADMIN);
    private final User borrower = user("borrower", Role.BORROWER);
    private AtomicReference<User> currentUser;
    private FakeStore store;
    private MaintenanceService service;

    private static User user(String name, Role role) {
        try {
            return new User(name, "hash", "salt", role);
        } catch (ValidationException e) {
            throw new AssertionError(e);
        }
    }

    @BeforeEach
    void setUp() {
        currentUser = new AtomicReference<>(admin);
        store = new FakeStore();
        Clock clock = Clock.fixed(Instant.parse("2026-11-23T09:00:00Z"), ZoneOffset.UTC);
        service = new MaintenanceService(store, currentUser::get, clock);
    }

    @Test
    void openAndCloseMaintenance() throws Exception {
        MaintenanceRecord opened = service.openMaintenance("EQ-1", "Repair lens");
        assertTrue(opened.isOpen());
        assertEquals("admin", opened.openedBy());
        assertEquals(1, service.listOpen().size());
        MaintenanceRecord closed = service.closeMaintenance(opened.id());
        assertFalse(closed.isOpen());
        assertEquals("admin", closed.closedBy());
        assertTrue(service.listOpen().isEmpty());
        assertThrows(InvalidOperationException.class,
                () -> service.closeMaintenance(opened.id()));
    }

    @Test
    void borrowerCannotOpenOrClose() throws Exception {
        MaintenanceRecord opened = service.openMaintenance("EQ-1", "Repair");
        currentUser.set(borrower);
        assertThrows(AuthorizationException.class,
                () -> service.openMaintenance("EQ-1", "New repair"));
        assertThrows(AuthorizationException.class,
                () -> service.closeMaintenance(opened.id()));
    }

    @Test
    void checkedOutItemCannotBeMaintained() {
        store.checkedOut = true;
        assertThrows(InvalidOperationException.class,
                () -> service.openMaintenance("EQ-1", "Repair"));
    }

    @Test
    void duplicateOpenIsRejected() throws Exception {
        service.openMaintenance("EQ-1", "Repair");
        assertThrows(InvalidOperationException.class,
                () -> service.openMaintenance("EQ-1", "Repair again"));
    }

    @Test
    void blankReasonAndUnknownEquipmentAreRejected() {
        assertThrows(ValidationException.class,
                () -> service.openMaintenance("EQ-1", " "));
        assertThrows(NotFoundException.class,
                () -> service.openMaintenance("missing", "Repair"));
    }

    private class FakeStore implements MaintenanceStore {
        MaintenanceRecord record;
        boolean checkedOut;

        public Equipment findEquipment(String id) throws Exception {
            if (!"EQ-1".equals(id)) return null;
            return new Equipment(id, "Camera", "Camera", "",
                    Condition.GOOD, OperationalStatus.OPERATIONAL);
        }
        public boolean hasOpenCheckout(String id) { return checkedOut; }
        public boolean hasOpenMaintenance(String id) {
            return record != null && record.isOpen();
        }
        public MaintenanceRecord findRecord(String id) {
            return record != null && record.id().equals(id) ? record : null;
        }
        public MaintenanceRecord open(String id, String reason, String admin, LocalDateTime time) {
            record = new MaintenanceRecord("M-1", id, reason, time, admin, null, null);
            return record;
        }
        public MaintenanceRecord close(String id, String admin, LocalDateTime time) {
            record = new MaintenanceRecord(record.id(), record.equipmentId(), record.reason(),
                    record.openedAt(), record.openedBy(), time, admin);
            return record;
        }
        public List<MaintenanceRecord> listOpen() {
            return record != null && record.isOpen() ? List.of(record) : List.of();
        }
        public List<MaintenanceRecord> listFor(String id) {
            return record != null && record.equipmentId().equals(id)
                    ? List.of(record) : List.of();
        }
    }
}
