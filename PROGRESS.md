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


