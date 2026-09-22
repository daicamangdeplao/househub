# Plan: PostgresML als Embedding-Service & PostgreSQL als Vektor-DB

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

**1c. Verhältnis zu `postgresql161`.** Die Compose-Datei enthält zusätzlich einen reinen `postgresql161`-Service (Port 5432, eigener Dockerfile-Build). Da die App auf 5434 (PostgresML) zeigt, laufen Embedding-Erzeugung (`pgml`) und Vektor-Speicherung (`vector`) aktuell in derselben PostgresML-Instanz. Das ist konsistent mit dem Ziel und in Ordnung; `postgresql161` wird für dieses Feature nicht benötigt.

Hinweis: PostgresML-Image ist groß (GPU/CPU-lastig); Tag-Version `2.10.0` ist gesetzt.

### 2. Embedding-Dimension verifizieren (häufige Fehlerquelle)
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
1. `command:`-Override in `postgresml210` entfernen + `.env` bereitstellen (Umgebung tatsächlich startfähig machen).
2. `truncate` entfernen + `spring.sql.init.mode` überdenken.
3. Dimension (768) gegen das reale Modell verifizieren.
4. Distanz-Metrik zwischen Index und Query angleichen.
5. Code-Cleanup (Old-Type, sync/async, Properties).
