# KashHelper Paper Example

This example now demonstrates persistence with a concrete **homes** domain model.

## What it demonstrates

- `JSON` backend using `JsonUtil`
- `SQL` backend using real typed columns and dialect-aware statements
  - `SQLITE`
  - `MARIADB`
  - `POSTGRESQL`
  - `MYSQL`
- `MONGODB` as optional backend (falls back if provider/driver is not available)

## Home model

`HomeRecord` stores:

- owner UUID
- home name
- world
- x, y, z
- yaw, pitch
- favorite flag
- updatedAt

## SQL statements actually used

The SQL implementation uses real statements (not blob key/value):

- `CREATE TABLE IF NOT EXISTS homes (...)`
- `CREATE INDEX IF NOT EXISTS idx_homes_owner ON homes(owner_uuid)`
- `SELECT ... WHERE owner_uuid = ? AND home_name = ?`
- `SELECT ... WHERE owner_uuid = ? ORDER BY favorite DESC, home_name ASC`
- dialect-aware `UPSERT` (`ON DUPLICATE KEY`, `ON CONFLICT`, `INSERT OR REPLACE`)
- `DELETE ... WHERE owner_uuid = ? AND home_name = ?`
- `SELECT owner_uuid, COUNT(*) ... GROUP BY owner_uuid ORDER BY total DESC LIMIT ?`

## Config files

- `config.yml` — general example settings
- `persistence.yml` — backend selection for locale + homes repositories
- `locales/en_us.yml` and `locales/fr_fr.yml` — locale bundles

## Backend presets

### JSON only

```yaml
# persistence.yml
type: JSON
fallbacks: []
json:
  folder: storage
```

### SQLite with JSON fallback

```yaml
# persistence.yml
type: SQL
fallbacks: [JSON]
sql:
  driver: SQLITE
  database: plugins/Example/example.db
  user: ""
  password: ""
```

### MariaDB with JSON fallback

```yaml
# persistence.yml
type: SQL
fallbacks: [JSON]
sql:
  driver: MARIADB
  host: localhost
  port: 3306
  database: example
  user: root
  password: root
```

### Mongo provider if present, fallback SQL then JSON

```yaml
# persistence.yml
type: MONGODB
fallbacks: [SQL, JSON]
```

## In-game demo commands

```text
/example persistence
/example persistence reload

/example home set <name>
/example home tp <name>
/example home list
/example home fav <name> [true|false]
/example home delete <name>
/example home top [limit]
/example home reload

/example lang <locale|reset>
```

## Build

```bash
cd /home/bramsou/Kash/KashHelper
./gradlew ':kash-helper-paper:example:build'
```
