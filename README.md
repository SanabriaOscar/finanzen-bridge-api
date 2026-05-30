# finanzen-bridge-api

Monorepo del puente POS local (backend + frontend en un solo repo).

## Estructura

```
finanzen-bridge-api/
├── pom.xml              # Agregador Maven (módulo backend)
├── backend/             # Spring Boot 21 — hexagonal, WebSocket, .exe
│   ├── pom.xml
│   └── src/
├── frontend/            # Angular admin panel (estilos corp)
│   ├── package.json
│   └── src/
└── README.md
```

## Build JAR (backend + UI embebida)

Desde la raíz del repo o desde `backend/`:

```powershell
cd finanzen-bridge-api/backend
mvn clean package -Pwith-ui
```

JAR: `backend/target/finnanzen-bridge-backend-1.0.0-SNAPSHOT.jar`

```powershell
java -jar backend/target/finnanzen-bridge-backend-1.0.0-SNAPSHOT.jar
```

## Build `finanzen-bridge.exe`

```powershell
cd finanzen-bridge-api/backend
mvn clean package -Pwith-ui,win-exe
```

Salida: `backend/dist/finnanzen-bridge-1.0.0.exe`

## Frontend solo (desarrollo)

```powershell
cd finanzen-bridge-api/frontend
npm install
npm start
```

Panel en http://localhost:4300 (requiere bridge en :9095 para WebSocket).

## Tests backend

```powershell
cd finanzen-bridge-api/backend
mvn test
```

## Endpoints (runtime)

| Recurso | URL |
|---------|-----|
| Panel admin | http://127.0.0.1:9095/ |
| WebSocket | ws://127.0.0.1:9095/ws?token=finnazen-bridge-local-dev |
| REST status | http://127.0.0.1:9095/api/bridge/status |
