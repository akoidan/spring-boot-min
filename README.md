
### Requirement
 - java-17-openjdk
 - gradlew
 - docker, docker-compose

Create `.env` file
```env
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppassword
POSTGRES_DB=appdb
JWT_SECRET=change-me-to-a-long-random-secret-change-me-to-a-long-random-secret
```


```bash
source_it () { 
    while read -r line; do
        if [[ "$line" =~ [A-Za-z] ]] && [[ $line != \#* ]]; then
            export "$line";
        fi;
    done < $1
}

source_it .env
```

```bash
docker volume create pg_data
docker compose up -d 
gradle liquibaseUpdate
gradle bootRun
```

### API doc

http://localhost:8080/swagger-ui/index.html



### Why I hate springboot/java
 - classnotfound exception
 - global scope for springboot services (no local isolated e.g. like in nestjs)
 - global scope of libs (lib A 1.0 depends on Lib b 1.0, lib C depends on Lib b 1.1) unless different classload pain
 - gradle project is only detected in IDEA when you remove .idea and reopen it 
 - default policy of hardcoding default env var values directly into code 
 - build.gradle looks like a pile of trash, gathering everything in a single file
 - liquibase is trash, required speciying dependency tree manually (how the fuck should I knw it?)
 - stupid global security config, no isolation, ideally each controller should have incapsulated security 