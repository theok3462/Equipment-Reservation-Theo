package equipmentReservation.view;

import equipmentReservation.controller.CheckoutReturnController;
import equipmentReservation.model.Condition;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.admin.CheckoutRecord;
import equipmentReservation.model.admin.CheckoutService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class CheckoutReturnView extends JFrameView {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final CheckoutService service;
    private final CheckoutReturnController controller;
    private final JTextField search = new JTextField(18);
    private final JComboBox<ReservationStatus> status =
            new JComboBox<>(ReservationStatus.values());
    private final JComboBox<Condition> returnCondition = new JComboBox<>(Condition.values());
    private final DefaultTableModel checkoutRows = rows(
            "Reservation", "Borrower", "Item", "Start", "Pick up by", "Status");
    private final DefaultTableModel returnRows = rows(
            "Reservation", "Borrower", "Item", "Checked out at");
    private final DefaultTableModel overdueRows = rows(
            "Reservation", "Borrower", "Item", "Scheduled end", "Overdue by");
    private final JTable checkoutTable = new JTable(checkoutRows);
    private final JTable returnTable = new JTable(returnRows);
    private final JLabel message = new JLabel(" ");

    public CheckoutReturnView(CheckoutService service, CheckoutReturnController controller) {
        super(service, controller);
        this.service = service;
        this.controller = controller;
        controller.setView(this);
        setTitle("Checkouts and Returns");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();

        JPanel checkout = new JPanel(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Search"));
        filters.add(search);
        filters.add(new JLabel("Status"));
        status.setSelectedItem(ReservationStatus.ACTIVE);
        filters.add(status);
        JButton find = new JButton("Find");
        find.addActionListener(e -> refreshReservations());
        filters.add(find);
        checkout.add(filters, BorderLayout.NORTH);
        checkout.add(new JScrollPane(checkoutTable), BorderLayout.CENTER);
        JButton checkoutButton = new JButton("Check Out Selected");
        checkoutButton.addActionListener(e -> checkOut());
        checkout.add(checkoutButton, BorderLayout.SOUTH);
        tabs.add("Checkout", checkout);

        JPanel returns = new JPanel(new BorderLayout());
        returns.add(new JScrollPane(returnTable), BorderLayout.CENTER);
        JPanel returnActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        returnActions.add(new JLabel("Return condition"));
        returnActions.add(returnCondition);
        JButton returnButton = new JButton("Record Return");
        returnButton.addActionListener(e -> recordReturn());
        returnActions.add(returnButton);
        returns.add(returnActions, BorderLayout.SOUTH);
        tabs.add("Return", returns);

        JPanel overdue = new JPanel(new BorderLayout());
        overdue.add(new JScrollPane(new JTable(overdueRows)), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> controller.operation("refresh"));
        overdue.add(refresh, BorderLayout.SOUTH);
        tabs.add("Overdue", overdue);

        add(tabs, BorderLayout.CENTER);
        add(message, BorderLayout.SOUTH);
        refreshAll();
    }

    private static DefaultTableModel rows(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public void refreshAll() {
        refreshReservations();
        try {
            returnRows.setRowCount(0);
            for (CheckoutRecord checkout : service.listOpenCheckouts()) {
                returnRows.addRow(new Object[]{checkout.reservationId(),
                        checkout.borrower(), checkout.equipmentId(),
                        format(checkout.checkedOutAt())});
            }
            overdueRows.setRowCount(0);
            for (CheckoutService.Overdue overdue : service.listOverdue()) {
                CheckoutRecord checkout = overdue.checkout();
                overdueRows.addRow(new Object[]{checkout.reservationId(),
                        checkout.borrower(), checkout.equipmentId(),
                        format(checkout.scheduledEnd()), duration(overdue.overdueBy())});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void refreshReservations() {
        try {
            checkoutRows.setRowCount(0);
            for (Reservation reservation : controller.search(search.getText(),
                    (ReservationStatus) status.getSelectedItem())) {
                checkoutRows.addRow(new Object[]{reservation.getId(),
                        reservation.getBorrower().getUsername(),
                        reservation.getItem().getName(),
                        format(reservation.getStart()),
                        format(service.getPickupDeadline(reservation)),
                        reservation.getStatus()});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void checkOut() {
        int row = checkoutTable.getSelectedRow();
        if (row < 0) return;
        try {
            String id = String.valueOf(checkoutTable.getValueAt(row, 0));
            CheckoutRecord checkout = controller.checkOut(id);
            message.setText(id + " checked out at " + format(checkout.checkedOutAt()) + ".");
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void recordReturn() {
        int row = returnTable.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(returnTable.getValueAt(row, 0));
        Condition condition = (Condition) returnCondition.getSelectedItem();
        if (condition == Condition.DAMAGED && JOptionPane.showConfirmDialog(this,
                "The item will be marked Non-operational. Continue?",
                "Damaged Return", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            CheckoutRecord checkout = controller.recordReturn(id, condition);
            message.setText(id + " returned at " + format(checkout.returnedAt()) + ".");
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private static String format(LocalDateTime date) {
        return date == null ? "" : date.format(FORMAT);
    }

    private static String duration(Duration d) {
        long minutes = d.toMinutes();
        return (minutes / 60) + " h " + (minutes % 60) + " min";
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Checkout and Return", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void modelChanged(ModelEvent event) {
        SwingUtilities.invokeLater(this::refreshAll);
    }
}
