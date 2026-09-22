# Progress Log - 25 Day Challenge

##Day 1: Project initialisation and Health check

### Completed Today 
- generated a spring boot project using spring initializr with dependencies: web, actuator, devtools. 
- fixed folder within folder issue and opened the projecit in IntelliJ IDEA correctly
- created the 'HelloController' with a 'GET /hello' endpoint
- ran application succesfully and verfified endpoint returns "Hello from Spring boot app!" in the browser

### Things implemented from "Spring Starts Here" book
- **Chapter 2 (beans and context):** @RestController is a sterotype annotation. When spring starts, it scans for this annotation and creates a "Bean" (instance of the class) inside the IoC container.
- **Chapter 7 (Spring MVC / DispatcherServlet):** Understanding the Spring MVC (model-view-controller) archeitecure. When i typed https://localhost:8080/hello , the 'Dispatcher servlet' intercepts the http request from the client. It looks at the 'GetMapping("/hello")' on my method and 'Handler mapping' to find 'sayHello()' method
- **Chapter 10 (REST):** '@RestController' is a specialized version of '@Controller' that combines '@Controller' and '@ResponseBody'. The method returns raw data directly into the HTTP response body rather than looking for a view like an html file.


##Day 2: Create the produt pojo (plain old java object) and add in-memory 'GET /api/products' endpoints. Constructor injection. 

###Completed today 
- created new package 'com.example.orders.prodcut' for all product-related classes.
- built 'Product.java' - plain POJO with private fields (id, name, price), constructor and getters.
- built 'ProductService.java' - use annotation '@Service', holds a 'Map<Long, product' (HashMap) as in-memory storage instead of a List, because findById needs direct key lookup rather than looping through a list.
- constructor initialised the map with 4 hardcoded products '.put(id, product)'
- implemented 'findAll()'. Returns all map values wrapped in a new ArrayList
- implemented 'findById(Long id)' - uses 'Optional.ofNullable(products.get(id))' to safely handle missing ids
- built 'ProductController.java'. Annotated '@RestController' + 'RequestMapping("/api/products")' using constructor injection
- implented 'GET /api/products'. calls 'findById()',unwraps the Optional with '.orElseThrow()'
- verified all three URLs: `/api/products` (returns array), `/api/products/1` (returns single product), `/api/products/99` (throws, confirms error path)
- saw the full stack trace in the terminal for the bad-id case — `NoSuchElementException: No value present` at `Optional.orElseThrow()`, bubbling up through Spring's DispatcherServlet to a 500 response

##Day 3: HTTP error semantics — mapping exceptions to proper status codes.

###Completed today: 
- identified that `ProductController.findById()`'s `.orElseThrow()` (no arguments) was throwing a plain `NoSuchElementException`, which Spring has no built-in mapping for, so it defaulted to a 500 Internal Server Error
- imported `org.springframework.http.HttpStatus` and `org.springframework.web.server.ResponseStatusException`
- changed `.orElseThrow()` to `.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id))` — using the supplier overload so the exception is only constructed lazily, on the empty-`Optional` path
- restarted the app and re-tested `/api/products/999` — confirmed the response is now a proper `404`, with Spring's `ResponseStatusExceptionResolver` logging a `WARN`-level "Resolved [...]" line instead of a full `ERROR`-level stack trace
- tested a second failure mode — malformed input (`/api/products/abc`) — and found Spring already returns `400 Bad Request` automatically, with no code required
- hit a WSL vs Windows networking snag (curl from WSL couldn't reach the app since it was running as a native Windows process via PowerShell, in a separate network namespace) — resolved by testing directly from PowerShell instead
- discovered PowerShell's `curl` is aliased to `Invoke-WebRequest`, which throws a terminal exception on non-2xx responses instead of returning the body — confirmed the underlying 400 status via the exception summary, understanding real curl (and Postman) would show the JSON body directly



###Things implemented



##Day 4: Modeling Order-Product — request DTOs, cross-service resolution, and BigDecimal totals

###Completed today: 
- chose the line-item pattern (`Order` holds `List<OrderItem>`, each wrapping a `Product` + `quantity`) over a bare `List<Product>` on `Order`, so quantity-per-product can actually be represented
- created `com.example.orders.order` package
- built `OrderItemRequest` and `CreateOrderRequest` as Java `record`s — the client-facing shape (bare `productId` + `quantity`), kept separate from the domain objects; caught and renamed an initial field called `request` (circular, self-referential naming) to `productId`, and `count` to `quantity` for domain-consistent naming
- built `OrderItem` as an immutable class (`final` fields, constructor-only assignment, no setters) holding a real `Product` reference + `quantity`
- built `Order` as an immutable class holding `id` + `List<OrderItem>`; decided `getTotal()` should be computed on-demand via a loop (`BigDecimal.multiply()`/`.add()`) rather than stored as a field, since `items` can't change after construction — guarantees the total can never go stale
- fixed several compile errors along the way: raw `List` vs typed `List<OrderItem>` in the constructor, wrong loop variable type (`Item` instead of `OrderItem`), and a case-sensitivity typo (`LineTotal` vs `lineTotal`)
- converted `Product`'s price field from `double` to `BigDecimal` in `ProductService`, ahead of needing it for `Order.getTotal()`
- built `OrderService`: `Map<Long, Order>` storage + `AtomicLong` id counter (mirroring `ProductService`), `findAll()`/`findById()`, and `createOrder(CreateOrderRequest)` — loops over incoming items, resolves each `productId` via `productService.findById(...).orElseThrow(NOT_FOUND)`, builds `OrderItem`s, stores and returns the new `Order`
- caught a bug where two separate attempts at the `Optional`-resolution logic were left in the same method (duplicate `product` variable, redundant double lookup) — cleaned up to a single `orElseThrow()` expression matching the Day 3 idiom
- built `OrderController`: `GET /api/orders`, `GET /api/orders/{id}` (mirroring `ProductController`), and `POST /api/orders` with `@RequestBody CreateOrderRequest`
- compared `@ResponseStatus(HttpStatus.CREATED)` vs a manually-built `ResponseEntity<Order>` — decided permanently on `@ResponseStatus`, since the status never varies here and there's no need for a `Location` header at this stage
- hit and fixed a `JAVA_HOME` issue in WSL blocking `./mvnw spring-boot:run` — found the existing JDK 21 via `update-alternatives --list java`, exported `JAVA_HOME`/`PATH`, persisted it in `~/.bashrc`
- verified the whole feature end-to-end via curl from WSL: `POST /api/orders` against a real seeded product resolved to the actual `Product` object (not just the echoed id) and computed the correct total (`89.99 × 2 = 179.98`); `POST /api/orders` with a nonexistent `productId` correctly returned `404`
- discovered `ProductController` has no `POST` endpoint yet (product data is seeded via constructor) — `POST /api/products` currently returns `405 Method Not Allowed`

###Things implemented
- **Request DTO vs domain object separation:** the shape a client sends (`OrderItemRequest`: bare `productId` + `quantity`) is deliberately a different type from what's stored internally (`OrderItem`: a resolved `Product` + `quantity`) — the client never has, and shouldn't send, a full `Product` object, only a reference to one.
- **Java `record`s:** used for the two request DTOs since they're pure, immutable data carriers — the language generates the constructor, field accessors (`.productId()`, not `.getProductId()`), `equals()`/`hashCode()`/`toString()` automatically.
- **`@RequestBody` and Jackson deserialization:** Spring Boot's `parameter-names` compiler module lets Jackson map incoming JSON fields onto a record's constructor parameters directly, with no `@JsonProperty` annotations needed.
- **`BigDecimal` arithmetic:** no operator overloading in Java, so totals are built with `.multiply()`/`.add()` instead of `*`/`+` — reinforced why floating-point types are the wrong tool for currency.
- **`@ResponseStatus` vs `ResponseEntity`:** `@ResponseStatus` fixes the response status once, at compile time, for the whole handler method. `ResponseEntity` decides it dynamically at runtime and allows setting headers (e.g. a `Location` header on a `201`). Chose the simpler, fixed option since this endpoint's outcome never varies.
- **Cross-service dependency resolution:** `OrderService` depends on `ProductService` via constructor injection — the same DI mechanism already used between `ProductController` and `ProductService`, reused one layer down to resolve `productId`s into real `Product` objects at order-creation time.

## Day 5: POST /api/products, Bean Validation, and cascading validation on orders

### Completed Today
- built `CreateProductRequest` as a Java `record` (`name` + `BigDecimal price`) — the client-facing shape for creating a product, with no `id` since the server assigns it (same DTO-vs-domain separation as Day 4)
- added `ProductService.createProduct(CreateProductRequest)`: builds a `Product` with a generated id, stores it in the map, returns it
- worked out the id-counter trap: the constructor seeds ids 1–4, so a counter starting at 0 would silently overwrite a seeded product on the first POST; seeded the `AtomicLong` from `maxId + 1`, computed from the map's keys (stream `.max()`), so it stays correct if more seed products are added later; compared `getAndIncrement()` (counter = next id to hand out) with `incrementAndGet()` (counter = last id handed out) — same first id, different starting value
- added `POST /api/products` to `ProductController` with `@RequestBody` + `@ResponseStatus(HttpStatus.CREATED)`; fixed a compile error (a comma between the parameter type and its name)
- ran the POST before the endpoint existed and got `405` (`HttpRequestMethodNotSupportedException`); after adding it, the same curl flipped to `201` with `"id":5`; before validation existed, a blank name, negative price, and missing price were all accepted and stored
- added `spring-boot-starter-validation` to `pom.xml`; IntelliJ showed "dependency not found" until the Maven project was reloaded
- got constraint placement wrong twice before fixing it: (1) `@NotBlank`/`@NotNull` above the `record` declaration applied to the whole class → `HV000030 UnexpectedTypeException`, and every request (even a valid one) returned `500`; (2) constraints on the wrong components (`@NotNull` on `name` lets `""` through, `@NotBlank` on a `BigDecimal` has no validator); final version: `@NotBlank` on `name`, `@NotNull @Positive` on `price`, and `@Valid` on the controller's `@RequestBody`
- verified via curl on WSL: blank / whitespace-only / missing name and negative / zero / missing price all return `400` (`MethodArgumentNotValidException`, logged with the field, rejected value, and message); malformed JSON returns `400` from a different exception (`HttpMessageNotReadableException`); a valid product returns `201`; rejected requests never reach the service, so no id is consumed and nothing invalid is stored
- extended validation to orders: `@Valid` on `OrderController.create`, `@NotEmpty` on `items`, `@NotNull` on `productId`, `@Positive` on `quantity`
- tested the order endpoint before adding the cascade: `quantity: -5` returned `201` with a total of `-449.95`; after cascading validation into the list, `-5`, `0`, and a bad second element (`items[1].quantity`) all return `400`, so every element is checked, not just the first
- found that a missing `quantity` never reaches validation: it's a primitive `int`, so Jackson rejects the `null` first (`Cannot map null into type int`) — still a `400`, but from `HttpMessageNotReadableException` rather than a constraint
- confirmed a nonexistent `productId` (999) is a business rule, not a format check, so no annotation catches it — it returns `404` from the `orElseThrow(...)` with `ResponseStatusException(NOT_FOUND)` already in `OrderService.createOrder` from Day 4
- hit a shell snag while testing: an unclosed quote in a curl `-d` argument hung the terminal, and pasting several commands at once tangled the output — run them one at a time

### Things implemented from resources
- **Bean Validation on record components:** constraint annotations go directly on the record components (inside the parentheses). Put above the `record` line, they apply to the whole object instead of a field. `@Valid` on the `@RequestBody` parameter is what triggers the checking; without it the annotations do nothing.
- **Which constraint fits which type:** `@NotBlank` only works on strings and rejects null, empty, and whitespace-only values; `@NotNull` only rejects null; `@Positive` treats `null` as valid, so a `BigDecimal` needs `@NotNull` alongside it; `@NotEmpty` rejects null and empty collections.
- **Cascading validation into collections:** `@Valid` on the controller parameter only validates the outer record. Constraints on the elements inside a `List` are skipped unless validation is cascaded into the list — field paths like `items[1].quantity` in the error output show the cascade worked.
- **Parse-time vs validation-time failures:** Jackson fails while reading the body (`HttpMessageNotReadableException`) before the object exists; Bean Validation runs after the object is built (`MethodArgumentNotValidException`). Spring's built-in `DefaultHandlerExceptionResolver` maps both to `400` (and an unsupported method to `405`), logged at WARN as "Resolved".
- **Reading validation logs:** each error names the field, the rejected value, and a default message; the `codes` list is a set of lookup keys for customizing messages later.
- **`AtomicLong` counter semantics:** `getAndIncrement()` returns then increments; `incrementAndGet()` increments then returns — the initial value has to match whichever is used, or the first generated id collides with a seeded one.
- **Primitive `int` in a request record:** a primitive can't be null, so an absent JSON field fails at deserialization instead of reaching validation.

### Next
- add a global exception handler (`@RestControllerAdvice`) so validation, parse, and not-found errors return one consistent JSON body instead of Spring's default — the current error responses also include a full stack `trace` field, so check where that comes from and turn it off
- move `NOT_FOUND` handling out of the service layer (`OrderService` currently throws an HTTP-aware `ResponseStatusException`)
