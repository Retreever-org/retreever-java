# Retreever

A **lightweight, developer-first toolkit** for automatically generating, visualizing, and testing API documentation in Spring Boot applications — **zero-config**, blazing fast, and built to stay out of your way.

Retreever plugs directly into your application, scans your controllers, models, and exception handlers, and produces a rich, structured API document ready for rendering inside a modern UI.


## ✨ Features

### 🚀 Zero Configuration

Drop it into your Spring Boot app — Retreever auto-discovers controllers, DTOs, exception handlers, and request/response schemas.

### 🧩 Smart Schema Resolution

Automatic JSON schema generation for:

* Request bodies (`@RequestBody`)
* Response bodies (`ResponseEntity<T>` and raw return types)
* Nested objects, arrays, enums, nulls
* Constraints via Jakarta Validation annotations

### 🛣️ Endpoint Introspection

Retreever inspects all endpoints and extracts:

* HTTP method & full path
* Path variables, query params, headers
* Media types (`consumes` / `produces`)
* Security flags (`@PreAuthorize`, custom `secured=true`)

### ❗ Error Mapping

Maps exception handlers (`@ExceptionHandler`) into structured error models, including:

* Status
* Description
* Error body schema

### 🧱 Clean, Stable Output Document

Everything is finally assembled into a strongly typed `ApiDocument` DTO containing groups, endpoints, schemas, examples, and metadata.


## 📦 Installation

*Coming soon once Maven Central publish is completed.*

After publishing, you will simply:

```xml
<dependency>
    <groupId>dev.retreever</groupId>
    <artifactId>retreever</artifactId>
    <version>1.0.0</version>
</dependency>
```


## 🛠 How It Works

Retreever consists of a clearly separated pipeline of resolvers and registries:

```
 ┌────────────────┐      ┌─────────────────────┐      ┌────────────────────────┐
 │ Controller     │      │ Endpoint Resolver    │      │ Schema Registry         │
 │ Scanner        ├─────►│ & Metadata Builders  ├─────►│ (JSON schema storage)  │
 └────────────────┘      └─────────────────────┘      └────────────────────────┘
          │                         │                           │
          ▼                         ▼                           ▼
 ┌────────────────┐      ┌─────────────────────┐      ┌────────────────────────┐
 │ Error Resolver │      │ Group Resolver       │      │ ApiDocument Assembler  │
 └────────────────┘      └─────────────────────┘      └────────────────────────┘
```

At runtime:

1. Spring Boot fires `ApplicationReadyEvent`.
2. Retreever scans all `@RestController` classes.
3. Each controller becomes an **ApiGroup**.
4. Each method becomes an **ApiEndpoint**.
5. DTOs and error models flow into the **SchemaRegistry**.
6. `ApiDocumentAssembler` creates the final JSON-ready structure.
7. The output is cached and served through `/retreever-tool`.


## 📄 Generated Output

The final document looks like:

```json
{
  "name": "API Documentation",
  "version": "v1",
  "groups": [
    {
      "name": "User APIs",
      "endpoints": [
        {
          "name": "Get User",
          "method": "GET",
          "path": "/users/{id}",
          "request": { ... },
          "response": { ... },
          "errors": [ ... ]
        }
      ]
    }
  ]
}
```

Clean, predictable, machine-readable.


## 🌐 Exposing the API

Retreever exposes your documentation via:

```
GET /retreever-tool
```

Returning the full `ApiDocument` JSON.

You can build any UI on top of this — React frontend, IDE plugin, browser extension, internal dashboard.


## 🧪 Project Status

* Core backend: **Complete**
* Schema engine: **Complete**
* API Document pipeline: **Complete**
* Frontend UI: **In progress**
* Microservice discovery support: **Planned**
* Additional type resolution (Map<K,V>, wildcards, multi-generic): **Planned**


## 🤝 Contributing

Contributions are welcome!

* Found a bug? Open an issue.
* Want to improve type resolution? PRs are appreciated.
* Have an idea? Submit a proposal.

Let's make API documentation fast, clean, and enjoyable.


## 📝 License

MIT License — free for personal and commercial use.


## ⭐ Acknowledgements

Built with ❤️ for developers who want tools that **stay out of the way and just work**.
