# Increment 09 equipment administration

Owner: Theo
Requirements: FR-03 and FR-20 through FR-23
Task instructions: prompts/09_equipment_admin.md

Prepared:
- EquipmentService with admin-only add, update and return-to-service.
- EquipmentAdminStore interface for persistence operations.
- ConflictException and InvalidOperationException.
- Field-aware ValidationException constructor.
- Six JUnit tests using an in-memory store.

To finish:
- Implement EquipmentAdminStore using the team's database and transaction API.
- Reject duplicate IDs at the database layer.
- Return-to-service must atomically update equipment and write service_log.
- Confirm open maintenance prevents returning to service.
- Prove reservation history remains unchanged after equipment edits.
- Run Gradle and persistence tests after the foundation is merged.
