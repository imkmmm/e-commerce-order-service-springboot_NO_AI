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

