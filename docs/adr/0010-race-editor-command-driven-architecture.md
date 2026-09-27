# ADR 0010: Use a command-driven race-editor backend with Clean Architecture

- Status: Accepted
- Date: 2026-09-27

## Context

We have decided to build the race-editor as a separate application module from the Java console app and separate from the future pre-race admin service. The next architectural decision is how the editor service itself should be structured internally.

The race-editor must support the following MVP operations during a race:

- list the current finishers for a selected race
- update a bib number from null to a valid numeric value
- update a bib number from a numeric value to a different numeric value
- update a bib number from a numeric value to null
- update a finish timestamp when the Enter key was pressed at the wrong time
- soft delete a duplicate finisher row when a runner re-enters and finishes again
- undelete soft-deleted row by mistake
- validate edits before they are persisted
- maintain a clear audit trail of corrections

The requirement is not merely to expose a form over the database. The domain has business rules, validation concerns, and operator safety requirements. A raw SQL layer would make it easy to introduce mistakes, hide validation intent, and create unmaintainable code.

## Domain invariants

- `deleted` is a first-class domain state.
- `deleted = true` and `deleted = false` are both valid operational states.
- Soft delete and undelete are both correction commands.
- `bib_number` and `timestamp` are editable only when `deleted = false`.
- `deleted` itself remains editable so a mistaken soft delete can be undone.

## Decision

The race-editor backend will use a command-driven architecture built around Clean Architecture boundaries.

The service will be structured as:

- UI layer
  - browser-based frontend
  - renders race rows and edit forms
  - calls the editor API

- Application layer
  - commands and use cases
  - validation rules
  - orchestration of domain logic
  - transaction boundaries

- Domain layer
  - entities
  - value objects
  - domain services
  - correction policies
  - validation logic
  - business rules for soft delete, timestamps, and bib updates

- Persistence layer
  - repository interfaces in the application/domain layers
  - concrete implementations in infrastructure
  - TimescaleDB access is isolated behind adapters

The backend will expose operations as commands rather than direct database updates. Examples include:

- `UpdateFinisherBibCommand`
- `UpdateFinisherTimestampCommand`
- `SoftDeleteDuplicateFinisherCommand`
- `UndeleteFinisherCommand`
- `ListRaceFinishersQuery`

Each command will be validated and handled by an application service or use case. The UI will not talk to TimescaleDB directly and will not contain domain logic.

## Rationale

This decision supports the requirements for:

- safety
- maintainability
- testability
- future extension
- separation of concerns

A command-driven API is a better fit than direct SQL writes because:

- edits are explicit and intentional
- validation can be enforced at the application/domain boundary
- one change in business rules is centralized instead of duplicated in UI code or SQL
- the code becomes easier to test with unit and integration tests
- audit behavior can be added consistently

Clean Architecture is appropriate because:

- the domain layer remains independent from infrastructure and UI
- database access, Kafka, and external services are implementation details
- future requirements such as approvals, role-based permissions, and audit logs can be introduced without rewriting the domain model

This also aligns with SOLID and the project coding guidelines:
- each class has a clear responsibility
- dependencies point inward
- persistence is abstracted behind interfaces
- logic is centralized instead of spread across UI and database code

## Scope

This ADR is intentionally scoped to the race-editor service.

It does not decide:
- the eventual frontend framework
- the eventual backend language or runtime
- the final authorization model
- the exact audit log storage mechanism
- whether pre-race admin is a separate runtime service beyond the current decision

Those topics remain open and should be addressed separately when needed.

## Consequences

### Positive consequences

- Operators are not required to write SQL
- Correction logic is centralized and easier to reason about
- Validation can be consistent across all edit operations
- Domain rules are testable without database wiring
- The editor can be extended with approval, audit, and permission logic later
- The system becomes easier to maintain as race administration grows

### Negative consequences

- More upfront structure is required than a quick direct SQL update path
- The UI/backend split introduces more code than a small local script
- We must maintain clear contracts between UI, application, and persistence layers

## Alternatives considered

### 1. Direct SQL updates from the UI or a service layer

This is the current practice and is the simplest to start, but it is unsafe and hard to validate. It also exposes business rules to the database layer and requires operator knowledge of SQL.

Rejected because it violates the operational safety requirement and is difficult to extend.

### 2. CRUD-style repository API without command-driven use cases

This would reduce some duplication, but it would still leave validation and correction policy spread across shallow service methods. It is not expressive enough for workflow-heavy edits such as soft-deletes, timestamp corrections, and rule-based validation.

Rejected because it is not sufficiently domain-driven.

### 3. Put all logic in the UI

This would make the frontend responsible for data integrity and domain logic. That would tightly couple the user interface to the business rules and make testing and reuse harder.

Rejected because it violates Clean Architecture and separation of concerns.

## Implementation guidance

The race-editor should follow a layered structure similar to:

- `apps/race-editor-ui`
- `apps/race-editor-api`
- `libs/race-editor-domain`
- `libs/race-editor-application`
- `libs/race-editor-persistence`

The core rule is:

- UI sends commands
- application layer validates and orchestrates
- domain owns business rules
- repository interfaces are used for persistence

This is intentionally a domain-first design that supports future race administration features without forcing them into the same process or code path.

## Related ADRs

- ADR 0009: Keep race administration in the same repo, but separate by app and service boundary
- ADR 0008: Use TimescaleDB as Primary Application Database

## Notes

This ADR is the first explicit design decision for the race-editor service itself. It is intentionally narrow and aims to give us a safe, testable, extendable foundation for live race corrections without locking in unrelated decisions for future pre-race admin capabilities.