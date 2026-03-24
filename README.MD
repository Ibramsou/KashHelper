# KashHelper

## Overview
KashHelper is split by platform modules, with persistence now included directly in `kash-helper-common`.

## Current Structure
- `common` -> `:kash-helper-common` (platform-agnostic helpers + persistence)
- `config` -> `:kash-helper-config`
- `platforms/paper` -> `:kash-helper-paper`
- `platforms/jda` -> `:kash-helper-jda`
- `platforms/hytale` -> `:kash-helper-hytale`
- `examples/paper` -> `:kash-helper-paper:example`

## Persistence Runtime Dependencies
Persistence APIs are in `:kash-helper-common`, but backend libraries are **not bundled**.
If you use SQL/JSON/Mongo persistence features, add the runtime libraries in your plugin project.

Example (consumer plugin):
```groovy
dependencies {
    compileOnly 'xyz.imrine:kash-helper-common:2.0.0-SNAPSHOT'

    // Add what you need for persistence runtime:
    runtimeOnly 'com.fasterxml.jackson.core:jackson-databind:2.20.0'
    runtimeOnly 'com.zaxxer:HikariCP:5.0.1'
    runtimeOnly 'org.xerial:sqlite-jdbc:3.46.0.1'
    // runtimeOnly 'mysql:mysql-connector-java:8.0.29'
    // runtimeOnly 'org.postgresql:postgresql:42.7.4'
    // runtimeOnly 'org.mongodb:mongodb-driver-sync:5.3.1'
}
```
