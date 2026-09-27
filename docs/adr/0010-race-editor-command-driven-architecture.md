# ADR 0010: Use a command-driven race-editor backend with Clean Architecture

- Status: Accepted
- Date: 2026-09-27

## Context

The race-editor is a separate application concern from the Java console app and from the future pre-race admin service. It is a browser-based, multi-user operational tool used during a race to correct finisher data without writing SQL.

The editor shares the same ecosystem as the console app and the same database, but it is not the same workflow. The console app is optimized for live event capture; the editor is optimized for correction, validation, auditability, and safe operator workflows.

The editor must support:
- race-scoped correction during a race
- safe mutation of finisher data
- explicit audit of changes
- future extension to broader race administration without coupling the whole system to the editor

The MVP scope is intentionally narrow: one race at a time, with a browser UI and a backend API.

## Decision

We will implement the race-editor as a separate application module with a command-driven backend built around Clean Architecture boundaries.

The service will be structured as:

- UI layer
  - browser-based frontend
  - renders race rows and edit forms
  - calls the editor API

- Application layer
  - commands and use cases
  - validation
  - orchestration of corrections
  - transaction boundaries

- Domain layer
  - finisher correction rules
  - state transitions
  - validation policies
  - domain invariants
  - audit-oriented business logic

- Persistence layer
  - repository interfaces in the inner layers
  - concrete TimescaleDB implementations in infrastructure
  - no direct database writes from the UI

The editor backend will expose operations as commands instead of ad hoc SQL updates. Examples include:
- `UpdateFinisherBibCommand`
- `UpdateFinisherTimestampCommand`
- `SoftDeleteFinisherCommand`
- `UndeleteFinisherCommand`

The UI will not directly manipulate the database. It will send commands to the backend, and the backend will validate and apply domain rules before persisting changes.

## Rationale

This architecture is the correct fit for the project requirements because:

- it keeps the UI and database decoupled
- it enforces validation and business rules in one place
- it supports audit, safety, and operator workflows
- it is suitable for a multi-user browser app
- it keeps the core domain independent from the database and web layer
- it avoids the fragility of raw SQL updates
- it allows future extension into pre-race admin or broader race operations without reworking the product boundary

This decision also aligns with Clean Architecture, SOLID, and the project’s intent to keep the editor maintainable and extensible.

## Scope

This ADR is intentionally scoped to the race-editor service architecture.

It does not decide:
- the exact frontend framework
- the exact backend language or runtime
- the final authorization model
- the exact audit persistence format
- the broader pre-race admin architecture

Those topics can be addressed in later ADRs as needed.

## Consequences

### Positive consequences

- Correction logic is explicit and centralized
- Operators do not need to write SQL
- Validation is safer and easier to test
- The UI remains decoupled from the database
- The backend can support audit and role-based rules later
- The system is more maintainable as the admin capability grows

### Negative consequences

- More structure and ceremony than a small direct SQL approach
- Additional service boundaries and contracts to maintain
- The team must keep the UI, application, and domain layers disciplined

## Alternatives considered

### 1. Direct raw SQL updates from the editor or a service

This is the current dangerous pattern. It is simple to start but makes the system fragile and hard to reason about.

Rejected because it exposes database logic to operators and makes validation and correction flows unsafe.

### 2. Put all logic in the UI

This would couple validation and business rules to the browser and make the domain harder to test and extend.

Rejected because it violates Clean Architecture and separation of concerns.

### 3. Keep the editor as a utility inside the Java console app

This would blur responsibilities and make the editor unsuitable for multi-user browser operation.

Rejected because it does not match the required workflow or product boundary.

## Related ADRs

- ADR 0009: Keep race administration in the same repo, but separate by app and service boundary
- ADR 0011: Define the race-editor domain model and MVP use cases

## Notes

This ADR is intentionally about the internal structure of the race-editor. The domain-level rules and correction workflow are defined separately in ADR 0011 so that each ADR has a clear purpose and does not become a mixed bag of architecture and business rules.