# Client ADRs

Short notes on why the client stack is what it is. One file per choice,
each ~10 lines. Read top-to-bottom for a stack tour.

- [compose-multiplatform](compose-multiplatform.md) — share UI across Android & iOS
- [decompose-navigation](decompose-navigation.md) — navigation & component lifecycle
- [koin](koin.md) — DI
- [ktor-client](ktor-client.md) — HTTP
- [multiplatform-settings](multiplatform-settings.md) — small key/value storage (tokens)
- [room-offline-first](room-offline-first.md) — single shared Room DB for offline-first lists/writes
- [custom-design-system](custom-design-system.md) — design tokens vs raw Material3
- [feature-module-split](feature-module-split.md) — `shared/features` + `composeApp/features`
- [use-case-layer](use-case-layer.md) — ViewModel → UseCase → repository
- [self-registering-plugins](self-registering-plugins.md) — sink + facade + `bind`
- [expect-actual](expect-actual.md) — platform abstractions
