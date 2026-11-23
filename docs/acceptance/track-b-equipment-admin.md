# Track B equipment administration acceptance tests

These cases are planned, not yet executed. Attach actual screenshots and test results after the Swing screens and database are connected.

| Case | Requirement | Action | Expected |
| --- | --- | --- | --- |
| 01 | FR-20 | Admin adds an item | Saved and visible in catalog |
| 02 | FR-20 | Borrower adds or edits | Access denied |
| 03 | FR-21 | Add duplicate equipment ID | Error, original unchanged |
| 04 | FR-21 | Leave required fields blank | Field error, no save |
| 05 | FR-21 | Add whitespace around values | Values trimmed |
| 06 | FR-22 | Update name and condition | Same ID, new details |
| 07 | FR-22 | Update unknown ID | Not found |
| 08 | FR-22 | Update item with reservations | History preserved |
| 09 | FR-23 | Return repaired item to service | Operational, service log saved |
| 10 | FR-23 | Return already-operational item | Rejected |
| 11 | FR-23 | Return with Damaged condition | Rejected |
| 12 | FR-23 | Return while maintenance open | Rejected |
| 13 | FR-23 | Fail during service-log write | Transaction rolled back |
| 14 | FR-03 | Signed-in user lists catalog | List and categories available |
| 15 | FR-20 | Signed-out user calls service | Access denied |
