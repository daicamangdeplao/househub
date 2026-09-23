# Plan: PostgresML als Embedding-Service & PostgreSQL als Vektor-DB

## Zielarchitektur (bewusste Trennung)

Gewünscht ist eine klare Trennung der Zuständigkeiten:

- **PostgresML (`postgresml210`, Port 5434)** → **ausschließlich** Embedding-Berechnung (`pgml`-Extension, Modell `distilbert-base-uncased`).
- **PostgreSQL (`postgresql161`, Port 5432)** → **ausschließlich** Vektor-Speicher (`vector`/pgvector-Extension, Tabelle `knowledge_base`, Ähnlichkeitssuche).

````mermaid
flowchart LR
    App[[HouseHub App]] -->|1. embed text| PGML[(PostgresML 5434\npgml)]
    PGML -->|Vektor 768| App
    App -->|2. INSERT / kNN-Suche| PG[(PostgreSQL 161 : 5432\nvector / pgvector)]
````

> **Wichtiger technischer Hinweis:** Die Spring-AI-Integration `spring-ai-starter-model-postgresml-embedding` ruft `pgml.embed(...)` über **die konfigurierte JDBC-Datasource** auf. Aktuell zeigt genau diese Datasource auf 5434. Für die Trennung müssen daher **zwei getrennte Datenbank-Verbindungen** existieren:
>
> - eine Datasource für die **Embedding-Berechnung** → PostgresML (5434),
> - eine (primäre) Datasource für **JPA/pgvector-Speicherung** → PostgreSQL 161 (5432).

## Overview

````mermaid
flowchart TD
    A([start]) --> B(Read File)
    B --> C(Chunk Content)
    C --> D(Vectorize)
    D --> E(Persist)
    E --> X([end])
````

## Analyse des aktuellen Stands

Das RAG-Grundgerüst ist bereits vorhanden und architektonisch korrekt:

- **build.gradle**: `spring-ai-starter-model-postgresml-embedding` (Spring AI BOM 1.0.0) + `com.pgvector:pgvector:0.1.6` + `org.postgresql:postgresql`.
- **application.properties**: DB zeigt auf eine PostgresML-Instanz (`jdbc:postgresql://localhost:5434/postgresml`); Embedding-Transformer konfiguriert (`distilbert-base-uncased`, `vectorType=PG_ARRAY`, `device=cpu`).
- **schema.sql**: Legt `vector`- und `pgml`-Extensions sowie die Tabelle `knowledge_base` mit `embedding vector(768)` und einem `ivfflat`-Index (cosine) an.
- **Entity/Repository**: `KnowledgeBaseJpaEntity` mappt `PGvector` über einen Custom-`@Type`; `KnowledgeBaseRepository.findMostSimilar` macht bereits Nearest-Neighbor-Suche (`<->`).
- **Persister**: Liest Dateien, chunked mit langchain4j, ruft `EmbeddingModel.embed(...)` (Spring AI → PostgresML) auf und speichert den Vektor.

**Fazit:** Die Architektur ist bereits die richtige. Es geht v. a. um Verifikation, Konsistenz-Fixes und eine reproduzierbare Umgebung.

## Empfohlene Schritte

### 1. PostgresML-Umgebung (erledigt – aber mit offenen Punkten)
Eine `docker-compose.yml` existiert bereits unter `C:\workspace\infrastructure\docker-compose.yml`. Sie enthält u. a. den Service `postgresml210`:

```yaml
postgresml210:
  image: ghcr.io/postgresml/postgresml:2.10.0
  container_name: postgresml210
  restart: always
  volumes:
    - postgresml_data:/var/lib/postgresql
  ports:
    - "5434:5432"
    - "8001:8000"
  stdin_open: true
  tty: true
  command: sudo -u ${POSTGRESML_USER} psql -d postgresml
```

Port `5434:5432` passt exakt zu `spring.datasource.url=jdbc:postgresql://localhost:5434/postgresml`. Damit ist Punkt 1 im Grundsatz erledigt. Es bleiben jedoch drei kritische Punkte:

**1a. `command:`-Override entfernen (kritisch).** Die Zeile `command: sudo -u ${POSTGRESML_USER} psql -d postgresml` überschreibt den Standard-Start des Containers. Statt den PostgreSQL-Server zu starten, öffnet der Container nur eine interaktive `psql`-Session (deshalb `stdin_open`/`tty`). Dadurch ist die DB von der App auf Port 5434 nicht erreichbar. Empfehlung: die `command:`-Zeile (sowie `stdin_open`/`tty`) entfernen, damit das Image den DB-Server per Default-Entrypoint startet. `psql` bei Bedarf via `docker exec -it postgresml210 psql -U postgresml -d postgresml` aufrufen.

**1b. Environment / `.env` bereitstellen.** Der Service setzt keine `POSTGRES_USER`/`POSTGRES_PASSWORD`/`POSTGRES_DB`; `${POSTGRESML_USER}` wird aus einer `.env`-Datei im Compose-Verzeichnis erwartet. Sicherstellen, dass `.env` existiert und die Werte zu den App-Defaults (`postgresml`/`postgresml`/`postgresml`) passen – sonst schlägt der Login fehl.

**1c. Rolle von `postgresql161` (jetzt zentral).** Die Compose-Datei enthält bereits den Service `postgresql161` (Port 5432, eigener Dockerfile-Build). Gemäß der Zielarchitektur wird **dieser** Service zum **Vektor-Speicher**. Dazu ist erforderlich:

- Die `vector`-Extension (pgvector) muss in `postgresql161` verfügbar sein. Ein reines `postgres:16.1` bringt sie nicht mit → im `./postgres-16.1/Dockerfile` `pgvector` installieren (z. B. Basis `pgvector/pgvector:pg16` oder Extension nachinstallieren) und in der DB `CREATE EXTENSION vector;` ausführen.
- Die Tabelle `knowledge_base` (inkl. `embedding vector(768)` + Index) wird in **dieser** 5432-Instanz angelegt (nicht mehr in PostgresML).
- `pgml` wird in `postgresql161` **nicht** benötigt (Embeddings kommen von PostgresML 5434).

**1d. Zwei Datasources konfigurieren (Kernpunkt der Trennung).** In `application.properties` muss die **primäre** Datasource auf `postgresql161` (5432) zeigen (JPA/Speicherung), und für die PostgresML-Embedding-Berechnung ist eine **zweite** Datasource auf 5434 nötig. Skizze:

```properties
# Primär: Vektor-Speicher (PostgreSQL 16.1 + pgvector)
spring.datasource.url=jdbc:postgresql://localhost:5432/househub
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}

# Sekundär: PostgresML nur für Embeddings
app.embedding.datasource.url=jdbc:postgresql://localhost:5434/postgresml
app.embedding.datasource.username=${POSTGRESML_USER}
app.embedding.datasource.password=${POSTGRESML_PASSWORD}
```

Da Spring AI standardmäßig die *primäre* Datasource für `postgresml.embed` verwendet, muss der `PostgresMlEmbeddingModel` explizit mit einem eigenen `JdbcTemplate` (auf Basis der 5434-Datasource) als Bean gebaut werden, damit Embedding-Berechnung (5434) und JPA-Speicherung (5432) sauber getrennt sind.

Hinweis: PostgresML-Image ist groß (GPU/CPU-lastig); Tag-Version `2.10.0` ist gesetzt.

### 2. `schema.sql` auf die 5432-Instanz ausrichten
Da der Speicher nun in `postgresql161` liegt, gehört `schema.sql` (Tabelle `knowledge_base`, `embedding vector(768)`, Index) gegen die **primäre** Datasource (5432) ausgeführt. Wichtig: **`create extension if not exists pgml;` aus `schema.sql` entfernen** – `pgml` gehört nur in die PostgresML-Instanz, nicht in den reinen Speicher. Es bleibt lediglich `create extension if not exists vector;`.

### 2b. Embedding-Dimension verifizieren (häufige Fehlerquelle)
Die Tabelle ist auf `vector(768)` festgelegt. Sicherstellen, dass `distilbert-base-uncased` tatsächlich 768 Dimensionen liefert. Bei Modellwechsel (z. B. `intfloat/e5-small` = 384) müssen sowohl `schema.sql` als auch `columnDefinition = "vector(768)"` in der Entity angepasst werden. Am besten in eine zentrale Konstante/Property auslagern.

### 3. `TRUNCATE` aus schema.sql entfernen
```sql
truncate table knowledge_base;
```
Zusammen mit `spring.sql.init.mode=always` wird die Tabelle bei jedem App-Start geleert. Für eine echte Vektor-DB unerwünscht.
- `truncate` entfernen, und
- langfristig auf **Flyway/Liquibase** statt `spring.sql.init` + `schema.sql` umsteigen (sauberere Migrationen, Extensions einmalig).

### 4. Distanz-Metrik konsistent halten
Der Index nutzt `vector_cosine_ops` (Cosinus), die Query nutzt aber `<->` (L2/Euklid). Für konsistente Ergebnisse:
- den Operator auf `<=>` (Cosinus) umstellen, oder
- den Index auf `vector_l2_ops` ändern.

Empfehlung: Cosinus-Operator `<=>` in `findMostSimilar` verwenden, passend zum Index.

### 5. Aufräumen / Konsolidierung
- `PGvectorTypeOld.java` entfernen und den ungenutzten synchronen Pfad in `Persister` (`persistKnowledge` vs. `persistKnowledgeAsync`) auf einen Weg festlegen (der Async-Weg passt zum konfigurierten `embeddingExecutor`).
- Hardcodierte Werte (`Topic.JAVA`, Chunk-Size 600/120) laut vorhandenen TODOs in Config/Properties auslagern.

### 6. Query-Embedding über denselben Service
Für die Suche die Anfrage mit demselben PostgresML-Modell einbetten wie die gespeicherten Dokumente (`embeddingService.embed(query)` → `findMostSimilar`). Sonst sind die Vektorräume inkompatibel.

## Prioritäten-Reihenfolge
1. `command:`-Override in `postgresml210` entfernen + `.env` bereitstellen (PostgresML als reinen Embedding-Dienst startfähig machen).
2. `postgresql161` als Vektor-Speicher vorbereiten: `pgvector` im Dockerfile installieren + `CREATE EXTENSION vector;`.
3. Zwei Datasources einführen: primär 5432 (JPA/pgvector), sekundär 5434 (PostgresML-Embedding), `PostgresMlEmbeddingModel` an die 5434-Datasource binden.
4. `schema.sql` auf 5432 ausrichten und `create extension pgml` daraus entfernen; `truncate` entfernen + `spring.sql.init.mode` überdenken.
5. Dimension (768) gegen das reale Modell verifizieren.
6. Distanz-Metrik zwischen Index und Query angleichen.
7. Code-Cleanup (Old-Type, sync/async, Properties).
