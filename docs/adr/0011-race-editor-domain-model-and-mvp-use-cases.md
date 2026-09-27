# ADR 0011: Define the race-editor domain model and MVP use cases

- Status: Accepted
- Date: 2026-09-27

## Context

The race-editor is a separate application module from the Java console app and is intended to support race-data correction during a race. The editor must allow operators to modify finisher data without writing SQL while preserving business correctness, auditability, and operator safety.

The current Java app already establishes a few important domain facts:

- a finisher bib number may be null
- the current integer parser accepts integer strings including negative and zero values
- whitespace is trimmed before parsing
- invalid numeric strings are treated as null rather than as valid values

This is the current parser behavior in the app and should be treated as the baseline domain contract until a different business rule is deliberately introduced.

The editor is scoped to one race at a time in the MVP, but the domain should remain race-aware and ready for later multi-race selection.

## Decision

We will define the race-editor as a domain-led correction workflow with an explicit finisher model and well-defined correction states.

The domain will include the following core concepts:

- `Finisher`
  - a finisher is the primary aggregate for the race-editor correction workflow
  - it has race identity, bib number, finish timestamp, and deletion state
  - it is the canonical domain object for applied corrections

- `BibNumber`
  - nullable integer value
  - validated according to the current app parser contract unless a stronger business rule is intentionally introduced later

- `FinishTimestamp`
  - editable timestamp
  - validated as a proper timestamp value

- `DeletedState`
  - first-class domain state
  - indicates whether the finisher is active or soft-deleted

- `AuditEntry`
  - records who changed what, when, and from/to which values
  - stored in a dedicated audit table

The domain will expose command-level operations for the editor, including:

- `ListFinishersForRaceQuery`
- `UpdateFinisherBibCommand`
- `UpdateFinisherTimestampCommand`
- `SoftDeleteFinisherCommand`
- `UndeleteFinisherCommand`

The following invariants define the correction domain:

- `deleted` is a first-class domain state
- a finisher may exist in a deleted state
- a deleted finisher remains visible to the operator for review and recovery
- a deleted finisher may be undeleted
- `bib_number` and `timestamp` are editable only when `deleted = false`
- `deleted` itself remains editable so a mistaken delete can be reverted
- all state-changing commands emit an audit entry

## Rationale

This ADR defines the business meaning of mutability and correction in the race-editor workflow. It is not just a UI list of fields; it is a decision about domain semantics and rules.

The decision is important because:

- the domain must explicitly model nullable bib values and soft-deleted rows
- the system must distinguish operational correction from physical deletion
- auditability is part of the domain, not just an implementation detail
- the same finisher rules must be shared between the console app and the race-editor to avoid drift
- the domain should remain stable even if the frontend technology changes later

This design also fits the architecture established in ADR 0010: the UI calls commands, the application layer validates and orchestrates, and the domain owns the real business invariants.

## Domain behavior

### 1. Bib number behavior

The current app contract allows a bib number to be null. It also treats non-null values as integer values, and the parser accepts negative or zero values unless a different business rule is intentionally added later.

Therefore, the domain rule for the MVP should be:

- `bib_number` is nullable
- if present, it must parse as an integer according to the current app semantics

If a stricter business rule such as “positive integer only” is desired later, it should be introduced deliberately and applied consistently across both the console app and the editor.

### 2. Timestamp behavior

The timestamp is mutable for correction when the operator hit Enter at the wrong moment. This is a genuine operational correction and should be modeled as a command-driven domain change instead of exposing a raw SQL update path.

### 3. Soft delete and undelete

Soft delete is a domain state change, not a physical removal. It is used to represent a row that should no longer be treated as active but should remain visible for review and correction.

The inverse operation is undelete, which restores the finisher to active state without requiring a full historical rewind. This is intentionally a state flip, not a version rollback.

### 4. Audit behavior

All state-changing operations should create an audit entry. This includes:

- bib changes
- timestamp changes
- soft delete
- undelete

The audit log should record:

- finisher identity
- race identity
- field name or state transition
- old value
- new value
- who changed it
- when it changed
- reason, if available

## Consequences

### Positive consequences

- The domain model matches the current application behavior
- Shared rules reduce duplication between the console app and the editor
- Correction operations are explicit, testable, and auditable
- The editor can safely support operational corrections without exposing raw SQL
- The domain can evolve toward stronger validation later without conflating current behavior with future policy

### Negative consequences

- The current parser behavior allows negative and zero values, which may not match desired human-facing business semantics
- A stronger domain rule such as positive-only bib numbers would require a deliberate rule change and migration of behavior across both apps
- Audit storage adds schema and operational complexity

## Alternatives considered

### 1. Execute raw SQL updates directly from the editor or service

This is the current dangerous pattern. It is simple but unsafe, opaque, and hard to validate.

Rejected because it makes the system fragile, exposes database mechanics to operators, and does not support a clean audit story.

### 2. Duplicate validation logic in both the Java app and the race-editor service

This would create drift and inconsistent rules over time.

Rejected because it violates the shared domain principle and increases maintenance burden.

### 3. Treat `deleted` as a database-only concern and not a recoverable domain state

Rejected because the system explicitly needs soft-delete and undelete workflows during race correction.

## Related ADRs

- ADR 0009: Keep race administration in the same repo, but separate by app and service boundary
- ADR 0010: Use a command-driven race-editor backend with Clean Architecture

## Notes

This ADR is intentionally limited to the domain behavior and correction rules of the race-editor. It is not a UI specification and is not a schema specification. It defines the architectural meaning of the correction workflow and the shared domain rules that must be enforced consistently across the console app and the editor.