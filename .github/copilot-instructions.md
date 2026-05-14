# Project general coding guidelines

## Code Style
- Use 4 spaces for indentation (Java standard)
- Use double quotes for strings
- End statements with a semicolon
- Place opening braces on the same line as the statement (e.g., `if (condition) {`)
- Limit lines to 100 characters for better readability
- **Operator spacing**: Always include a space before and after binary operators
  - Correct: `int result = x + y * 2;`
  - Avoid: `int result=x+y*2;`
- **No trailing commas**: Do not include commas after the last element in arrays or collections
  - Correct: `int[] numbers = {1, 2, 3};`
  - Avoid: `int[] numbers = {1, 2, 3,};`
- **Method calls and declarations**: No space between method name and parentheses
  - Correct: `calculateTotal()` and `public void process() {`
  - Avoid: `calculateTotal ()` and `public void process () {`
- **Control structures**: Include a space between keyword and parentheses
  - Correct: `if (condition) {` and `for (int i = 0; i < 10; i++) {`
  - Avoid: `if(condition) {` and `for(int i = 0; i < 10; i++) {`

## Naming Conventions

### Variables and Parameters
- Use camelCase (e.g., `userName`, `itemCount`)
- Use descriptive names that clearly indicate purpose
- Avoid single-letter names except for loop indices (`i`, `j`)
- **Abbreviations**: Avoid unless from the approved list below (e.g., prefer `identifier` over `id`, prefer `universallyUniqueIdentifier` over `uuid`)

### Methods
- Use camelCase (e.g., `calculateTotal()`, `getUserById()`)
- Prefix boolean methods with `is`, `has`, or `can` (e.g., `isValid()`, `hasPermission()`)
- Use verbs that describe the action
- **Abbreviations**: Avoid unless from the approved list below (e.g., prefer `getUniversallyUniqueIdentifier()` over `getUuid()`)

### Classes and Interfaces
- Use PascalCase (e.g., `UserService`, `PaymentProcessor`)
- Use nouns that describe the entity or responsibility
- **Abbreviations**: Avoid unless from the approved list below (e.g., prefer `JsonParser` over `JSONParser`, prefer `DataAccessObject` over `DAO`)

### Constants
- Use ALL_CAPS with underscores (e.g., `MAX_RETRY_ATTEMPTS`, `DEFAULT_TIMEOUT_MS`)
- Apply to static final fields and immutable values
- **Abbreviations**: Allowed from the approved list below (e.g., `DEFAULT_TIMEOUT_MS`, `MAX_HTTP_CONNECTIONS`)

### Approved Abbreviations (Variables, Methods, Classes, Constants)
Use only established abbreviations from these categories:
- **Official Java APIs**: `id`, `url`, `http`, `xml`, `json`
- **Common frameworks**: `dto`, `dao`, `mvc`
- **Industry standards**: `csv`, `uuid`, `jwt`, `oauth`
- **Project-specific**: (document in README or wiki)
- **When in doubt, spell it out** (e.g., prefer `identifier` over `id` in new code unless matching an API contract)

## Code Quality
- Write unit tests for critical functions and components to ensure code reliability
- Red, Green, Refactor: Follow the TDD approach to write tests before implementing functionality and refactor code as needed to improve readability and maintainability
- Use meaningful variable and method names that clearly describe their purpose
- Write modular and reusable code by breaking down complex functions into smaller, focused methods
- Avoid redundant comments that restate the code (e.g., `count++; // increment count`). Include comments only for:
  - **Non-standard algorithms or workarounds** (e.g., bitwise tricks, framework-specific hacks, performance optimizations that sacrifice clarity)
  - **Unintuitive business logic** that isn't self-evident from naming alone (e.g., why a particular calculation or validation order is required)
  - **External API quirks or constraints** that would surprise maintainers (e.g., "API returns null instead of empty list in edge cases")
  - **Rationale for architectural decisions** that might otherwise seem questionable (e.g., "Intentionally not caching to ensure fresh data per security requirement")
- Place comments near the code they describe and keep them concise (explain *why*, not *what*)
- Add error handling for user inputs and external API calls to ensure robustness and prevent crashes

## Design Principles
All SOLID principles are fundamental to this codebase. Prioritize them to enable decoupling and maintainability:

- Separate concerns into focused classes with single responsibilities
- Use interfaces (e.g., `Parser<T, R>`) to allow extension without modification
- Inject dependencies through constructors; depend on abstractions, not concrete implementations
- Create focused, minimal interfaces—avoid large God interfaces
- Ensure implementations are substitutable for their interface contracts

## Architecture
Maintain clear separation of concerns with distinct layers:

- Organize code into logical layers (UI, controllers, services, models, utilities)
- Outer layers depend on inner layers, never the reverse
- Keep layers thin—avoid duplicating business logic across layers
- Use dependency injection to manage cross-layer dependencies
- Encapsulate external integrations (Kafka, databases) behind service abstractions