# Track B integration notes

The project setup and MVC/domain skeleton from the group's main branch are present. The database, session, reservation services and application navigation were not present in the snapshot used to prepare this code.

## Dependencies to reconcile
- EquipmentService currently takes EquipmentAdminStore and a Supplier<User>. Connect to the team's authenticated Session and SQLite repository.
- SettingsService needs a settings-table repository; ensure the 30-minute default persists after restart.
- CheckoutStore must implement atomic expiration, checkout/return status transitions, open checkouts, and reservation search. Failed operations must roll back fully.
- MaintenanceStore must implement the maintenance_records table, block new reservations/checkout via reservation availability, and leave existing reservations unchanged.
- The master context expects ReservationSystem to be the single notifying Model. The current services notify directly as temporary standalone implementations. Refactor event delivery and the Swing view model wiring to the final facade.
- Reservation IDs and record types should match the actual merged schema; do not assume the draft String IDs are the final API.

## Testing before merge
1. Pull latest group main after core increments 05-08, 12, 14 and 19 land.
2. Adapt service/store interfaces to the actual Database transaction API and repositories.
3. Run Gradle build/test, then integration tests using a temporary SQLite file.
4. Test exact pickup deadline, duplicate returns, damaged returns, open maintenance, and persisted restart.
5. Exercise Swing screens on macOS and Windows. Capture error and confirmation dialogs.
6. Regenerate UML from merged code and update the equipment statechart.
7. Keep the original AI prompts, review with teammates and submit via a pull request.
