package equipmentReservation.view;

import equipmentReservation.controller.MaintenanceController;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.admin.EquipmentService;
import equipmentReservation.model.admin.MaintenanceRecord;
import equipmentReservation.model.admin.MaintenanceService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class MaintenanceView extends JFrameView {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final MaintenanceService maintenance;
    private final EquipmentService equipment;
    private final MaintenanceController controller;
    private final JComboBox<String> item = new JComboBox<>();
    private final JTextField reason = new JTextField(20);
    private final DefaultTableModel records = new DefaultTableModel(
            new Object[]{"Record", "Item", "Reason", "Started"}, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(records);

    public MaintenanceView(MaintenanceService maintenance, EquipmentService equipment,
            MaintenanceController controller) {
        super(maintenance, controller);
        this.maintenance = maintenance;
        this.equipment = equipment;
        this.controller = controller;
        controller.setView(this);
        setTitle("Maintenance");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 400);
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fields.add(new JLabel("Equipment"));
        fields.add(item);
        fields.add(new JLabel("Reason"));
        fields.add(reason);
        JButton open = new JButton("Open Maintenance");
        open.addActionListener(e -> openMaintenance());
        fields.add(open);
        add(fields, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JButton close = new JButton("Close Selected Record");
        close.addActionListener(e -> closeMaintenance());
        add(close, BorderLayout.SOUTH);
        refreshMaintenance();
    }

    public void refreshMaintenance() {
        try {
            Object selected = item.getSelectedItem();
            item.removeAllItems();
            for (Equipment equipmentItem : equipment.listEquipment()) {
                item.addItem(equipmentItem.getId());
            }
            if (selected != null) item.setSelectedItem(selected);
            records.setRowCount(0);
            for (MaintenanceRecord record : maintenance.listOpen()) {
                records.addRow(new Object[]{record.id(), record.equipmentId(),
                        record.reason(), record.openedAt().format(FORMAT)});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void openMaintenance() {
        if (item.getSelectedItem() == null) return;
        String equipmentId = String.valueOf(item.getSelectedItem());
        try {
            List<Reservation> affected = controller.activeReservationsFor(equipmentId);
            String explanation = affected.isEmpty()
                    ? "Open maintenance for " + equipmentId + "?"
                    : "These reservations stay active but cannot be checked out during maintenance.";
            JPanel confirmation = new JPanel(new BorderLayout());
            confirmation.add(new JLabel(explanation), BorderLayout.NORTH);
            if (!affected.isEmpty()) {
                DefaultTableModel rows = new DefaultTableModel(
                        new Object[]{"Reservation", "Borrower", "Start"}, 0);
                for (Reservation r : affected) {
                    rows.addRow(new Object[]{r.getId(), r.getBorrower().getUsername(),
                            r.getStart().format(FORMAT)});
                }
                confirmation.add(new JScrollPane(new JTable(rows)), BorderLayout.CENTER);
            }
            if (JOptionPane.showConfirmDialog(this, confirmation,
                    "Confirm Maintenance", JOptionPane.OK_CANCEL_OPTION)
                    != JOptionPane.OK_OPTION) return;
            controller.open(equipmentId, reason.getText());
            reason.setText("");
            refreshMaintenance();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void closeMaintenance() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        try {
            String id = String.valueOf(table.getValueAt(row, 0));
            controller.close(id);
            refreshMaintenance();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Maintenance", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void modelChanged(ModelEvent event) {
        SwingUtilities.invokeLater(this::refreshMaintenance);
    }
}
