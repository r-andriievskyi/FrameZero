# Use-case layer (ViewModel → UseCase → repository)

**Why:** ViewModels never touch repositories directly — they invoke
single-purpose `UseCase<Params, T>` / `NoParamsUseCase<T>` (see
`shared/.../domain/UseCase.kt`). `invoke()` wraps `execute()` in `runCatching`
and returns `Outcome<T>` (failures mapped to `DomainError`), so business actions
are small, testable, and never throw across a layer boundary.

**Not:**
- **ViewModel calls repository directly** — business logic leaks into the
  presentation layer and gets duplicated across screens.
- **try/catch in every ViewModel** — `invoke()` already returns `Outcome<T>`;
  re-wrapping is redundant.

**Cost:** one small class per business action. Each lives in the feature's
`domain/`/`usecase/` package and is registered `factory` in its Koin module.

