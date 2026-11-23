package equipmentReservation.view;

import equipmentReservation.controller.EquipmentAdminController;
import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.admin.EquipmentService;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class EquipmentManagementView extends JFrameView {
    private static final long serialVersionUID = 1L;
    private final EquipmentService service;
    private final EquipmentAdminController controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField id = new JTextField();
    private final JTextField name = new JTextField();
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextArea description = new JTextArea(3, 18);
    private final JComboBox<Condition> condition = new JComboBox<>(Condition.values());
    private final JComboBox<OperationalStatus> status =
            new JComboBox<>(OperationalStatus.values());
    private boolean newItem = true;

    public EquipmentManagementView(EquipmentService service, EquipmentAdminController controller) {
        super(service, controller);
        this.service = service;
        this.controller = controller;
        controller.setView(this);
        setTitle("Equipment Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 460);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectItem();
        });

        category.setEditable(true);
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("ID"));
        fields.add(id);
        fields.add(new JLabel("Name"));
        fields.add(name);
        fields.add(new JLabel("Category"));
        fields.add(category);
        fields.add(new JLabel("Description"));
        fields.add(new JScrollPane(description));
        fields.add(new JLabel("Condition"));
        fields.add(condition);
        fields.add(new JLabel("Status"));
        fields.add(status);

        JButton add = new JButton("New Item");
        JButton save = new JButton("Save");
        JButton returnService = new JButton("Return to Service");
        JButton settings = new JButton("Settings...");
        add.addActionListener(e -> controller.operation("new"));
        save.addActionListener(e -> saveItem());
        returnService.addActionListener(e -> returnItem());
        settings.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Open the Settings window from the administrator menu."));

        JPanel actions = new JPanel();
        actions.add(add);
        actions.add(save);
        actions.add(returnService);
        actions.add(settings);
        JPanel right = new JPanel(new BorderLayout());
        right.add(fields, BorderLayout.CENTER);
        right.add(actions, BorderLayout.SOUTH);
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table), right), BorderLayout.CENTER);
        refreshEquipment();
    }

    public void newItem() {
        newItem = true;
        id.setEditable(true);
        id.setText("");
        name.setText("");
        category.setSelectedItem("");
        description.setText("");
        condition.setSelectedItem(Condition.GOOD);
        status.setSelectedItem(OperationalStatus.OPERATIONAL);
        table.clearSelection();
    }

    public void refreshEquipment() {
        try {
            List<Equipment> list = service.listEquipment();
            tableModel.setRowCount(0);
            category.removeAllItems();
            for (String itemCategory : service.listCategories()) {
                category.addItem(itemCategory);
            }
            for (Equipment equipment : list) {
                tableModel.addRow(new Object[]{
                    equipment.getId(), equipment.getName(), equipment.getOperationalStatus()
                });
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void selectItem() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        try {
            Equipment equipment = service.getEquipment(String.valueOf(table.getValueAt(row, 0)));
            newItem = false;
            id.setText(equipment.getId());
            id.setEditable(false);
            name.setText(equipment.getName());
            category.setSelectedItem(equipment.getCategory());
            description.setText(equipment.getDescription());
            condition.setSelectedItem(equipment.getCondition());
            status.setSelectedItem(equipment.getOperationalStatus());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveItem() {
        try {
            controller.save(newItem, id.getText(), name.getText(),
                    String.valueOf(category.getSelectedItem()), description.getText(),
                    (Condition) condition.getSelectedItem(),
                    (OperationalStatus) status.getSelectedItem());
            refreshEquipment();
            JOptionPane.showMessageDialog(this, "Equipment saved.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void returnItem() {
        if (newItem || status.getSelectedItem() != OperationalStatus.NON_OPERATIONAL) {
            JOptionPane.showMessageDialog(this, "Select a non-operational item first.");
            return;
        }
        Object[] choices = {Condition.GOOD, Condition.FAIR};
        Condition selected = (Condition) JOptionPane.showInputDialog(this,
                "Choose the repaired item's condition:", "Return to Service",
                JOptionPane.QUESTION_MESSAGE, null, choices, Condition.GOOD);
        if (selected == null) return;
        try {
            controller.returnToService(id.getText(), selected);
            refreshEquipment();
            JOptionPane.showMessageDialog(this, "Item returned to service.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Equipment",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void modelChanged(ModelEvent event) {
        SwingUtilities.invokeLater(this::refreshEquipment);
    }
}
