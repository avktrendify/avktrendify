# avktrednify-service
This is the backend service that powers https://avktrendify.github.io

## Running
The values provided in `application.properties` for certain things are placeholders. You need to provide the right values.
- `--server.ssl.key-store=`
- `--server.ssl.key-store-password=`
- `--spring.datasource.url=`

## Deployment
Run `mvn package -DskipTests`.
Copy jar file (e.g. `avakinitemdb-0.0.1-SNAPSHOT.jar` in `target/` folder) to production machine