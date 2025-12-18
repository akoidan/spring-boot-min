
### Requirement
 - java-17-openjdk
 - gradlew
 - docker, docker-compose
Create `.env` file
```env
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppassword
POSTGRES_DB=appdb
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
docker compose up
gradle bootRun
```


### Why I hate springboot/java
 - classnotfound exception
 - global scope for springboot services (no local isolated e.g. like in nestjs)
 - global scope of libs (lib A 1.0 depends on Lib b 1.0, lib C depends on Lib b 1.1) unless different classload pain
 - gradle project is only detected in IDEA when you remove .idea and reopen it 