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



##Day 4: 


###Completed today: 
- 


###Things implemented
