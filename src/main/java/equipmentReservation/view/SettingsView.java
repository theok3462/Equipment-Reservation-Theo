package equipmentReservation.view;

import equipmentReservation.controller.SettingsController;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.admin.SettingsService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class SettingsView extends JFrameView {
    private static final long serialVersionUID = 1L;
    private final SettingsService settings;
    private final SettingsController controller;
    private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(30, 0, 1441, 1));

    public SettingsView(SettingsService settings, SettingsController controller) {
        super(settings, controller);
        this.settings = settings;
        this.controller = controller;
        controller.setView(this);
        setTitle("Reservation Settings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(360, 150);
        JPanel row = new JPanel(new FlowLayout());
        row.add(new JLabel("Pickup window (minutes):"));
        row.add(minutes);
        add(row, BorderLayout.CENTER);
        JButton save = new JButton("Save");
        save.addActionListener(e -> saveSetting());
        add(save, BorderLayout.SOUTH);
        refreshSetting();
    }

    public void refreshSetting() {
        try {
            minutes.setValue(settings.getPickupWindowMinutes());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Settings",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSetting() {
        try {
            controller.save((Integer) minutes.getValue());
            JOptionPane.showMessageDialog(this, "Settings saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Settings",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void modelChanged(ModelEvent e) {
        SwingUtilities.invokeLater(this::refreshSetting);
    }
}
