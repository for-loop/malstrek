# ADR 0009: Keep race administration in the same repo, but separate by app and service boundary

- Status: Accepted
- Date: 2026-09-27

## Context

The race system has two distinct operational concerns:

1. The race console application that captures live race events during the event
2. The race administration/editor application that corrects and manages result data during and after the race

These workflows share the same core ecosystem, especially the same TimescaleDB and Kafka-backed event pipeline, but they are not the same runtime responsibilities. The console app is optimized for fast event capture and operator input. The admin/editor app is optimized for validation, correction workflows, auditability, user operations, and future pre-race configuration.

The system also needs to grow beyond a single operator. As race complexity increases, it is likely that one operator uses the console app while another user or team edits/validates data from a browser. This creates a separate user experience and an authorization boundary.

We considered whether the editor should live in the same repository or a separate repository. We also considered whether the correction service and the future pre-race admin service should be the same runtime service.

The key architectural concern is not the database. The key concern is the product boundary and runtime responsibilities.

## Decision

We will keep the race administration capability in the same repository for the initial phase, but we will structure it as a separate application/module with a clear runtime boundary.

The repo will define the following top-level application areas:

- `apps/race-console/`
  - current Java-based console application
  - live event capture and race ingestion

- `apps/race-admin-api/`
  - backend API for correction workflows and admin operations
  - domain/application logic for editing race data
  - validation and business rules
  - audit logging and authorization hooks

- `apps/race-admin-ui/`
  - browser-based frontend for editing rows and managing race data
  - independent from database access
  - interacts only through the admin API

We will also separate shared capabilities into libraries, such as:

- `libs/race-domain/`
- `libs/race-application/`
- `libs/race-persistence/`
- `libs/race-contracts/`
- `libs/shared-infrastructure/`

This preserves one repository for product cohesion while enforcing separate boundaries for independent concerns.

We will also keep the race editor and any future pre-race admin functionality under the same overall "race administration" domain, but we will not assume they must be the same runtime service. They can be separate APIs or separate modules within the same product area if the workflows diverge later.

## Rationale

This decision balances the competing goals:

- Fast MVP iteration
- clear architectural separation
- future flexibility
- manageable repo structure

Keeping both app types in the same repo is useful because:
- they share infrastructure and schema assumptions
- they are part of the same race operations product
- local setup and CI are simpler
- we avoid premature repo fragmentation

Separating them into distinct apps/modules is necessary because:
- they have different user workflows
- they have different validation and audit requirements
- they may evolve at different speeds
- they should not be coupled by implementation details or UI concerns

This aligns with Clean Architecture:
- outer layers depend on inner layers
- domain logic is independent from UI and infrastructure
- the editor API is a separate application concern
- the DB and Kafka are implementation details, not domain logic

This also keeps the system maintainable as it grows to pre-race configuration, race setup, user administration, or approval flows.

## Consequences

### Positive consequences

- The repo stays cohesive and easier to navigate for a small team
- The race editor is not hidden inside the console app or treated as a utility script
- We can keep separate frontend, backend, and domain responsibilities
- We can support multi-user browser access without forcing the console app to become a web app
- We preserve a path to later splitting into a separate repo if the admin product grows significantly
- The architecture supports future extension to pre-race configuration and broader admin features

### Negative consequences

- The repo will contain multiple application types
- We must enforce boundaries and avoid accidental cross-coupling
- Some shared infra and environment concerns will be duplicated across apps
- The team must be disciplined about not mixing business logic between console and admin workflows

### Operational implications

- The admin API and UI will have their own local development flow
- The database schema and migrations must be shared intentionally
- Domain contracts should be versioned or carefully managed to avoid hidden breakage between apps

## Alternatives considered

### 1. Put the editor in a separate repository immediately

This would provide a stronger product boundary, but it adds unnecessary overhead for the initial phase. It is a better option only when:
- the admin tool becomes independently maintained
- there are multiple teams or separate product owners
- deployment and release cadences diverge materially

We rejected this as premature for the MVP.

### 2. Keep everything in a single monolithic repo without app separation

This is easy to start, but it encourages cross-coupling between the console app, admin UI, and domain logic. It makes the project harder to reason about as the admin workflow grows. It also makes Clean Architecture harder to enforce.

We rejected this because it does not scale well and creates a strong risk of accidental coupling.

### 3. Embed the editor directly inside the Java console app

This would simplify initial implementation, but it would blur responsibilities and make the editor unfit for browser-based multi-user access. It would also make the system harder to evolve toward a proper admin platform.

We rejected this because it violates the product boundary and the multi-user requirement.

## Implementation guidance

The repo structure will reflect this ADR.

At a minimum:
- keep `apps/race-console` and `apps/race-admin-*` as distinct deployment units
- keep domain logic in reusable libraries
- keep UI-to-API and API-to-domain boundaries explicit
- keep database access behind repository interfaces
- keep validation, correction policies, and audit rules in the application/domain layers

A future split to a separate repo remains possible without reworking the domain model if we keep the boundaries clean.

## Related ADRs

- ADR 0008: Use TimescaleDB as Primary Application Database

## Notes

This decision intentionally treats the admin/editor workflow as a distinct application capability, not as a sidecar utility attached to the console app. This is the correct boundary for a browser-based, multi-user, correction-oriented tool that must eventually support broader race administration.