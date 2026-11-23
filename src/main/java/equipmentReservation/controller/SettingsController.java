package equipmentReservation.controller;

import equipmentReservation.model.admin.SettingsService;
import equipmentReservation.view.SettingsView;

public class SettingsController extends AbstractController {
    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
        setModel(settings);
    }

    public void save(int minutes) throws Exception {
        settings.setPickupWindowMinutes(minutes);
    }

    @Override
    public void operation(String option) {
        if ("refresh".equals(option) && getView() instanceof SettingsView view) {
            view.refreshSetting();
        }
    }
}
