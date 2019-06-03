# exchange-rate-service
A service, that constantly checks the currency exchange rate from Bitcoin to US-Dollar (1 Bitcoin = x USD).

## Architecture
You may refer to the architecture [diagram](Architecture.pdf) for a bird view for this project. However of course, not everything is implemented. 
Here are the assumptions that have been dropped for simplicity:

- Physical database: Currently only in-memory solution. 
- Command Query Responsibility Segregation (CQRS): However it was mentioned in the architecture document to explain the vision, not the current implementation.
- Dynamic/database configurations: currently only property files used.
- All aspects outside the service scope in the architecture pdf document, are mentioned only to give some ideas about potential enhancements.   
### Project Layers
##### API Resources:
This layer is responsible for receiving http requests and deliver them to the managers layer, then use transformers (if necessary) to transform model objects to DTOs.

##### Transformers:
This layer is responsible for transforming entities(model)(to/from) DTOs.

##### Managers:
This is the business logic layer, it's responsible for fulfilling the logic needed to respond to API Resources. That's achieved by either delegating to downstream dependencies or DAOs/Repository layer.
Normally this layer has exposure to the model, clients and repositories.

##### Clients:
A wrapper classes that decorates/encapsulates calls to downstream services.

##### Repositories:
DAO layer, where all database related operations reside.

##### Workers:
Background workers are business logic layer that initiated by a scheduled jobs rather than an http request.
Normally this layer has exposure to the model, clients and repositories.

##### Exceptions Handling:
Responsible for handling business exceptions, and logging server errors -if any-.

