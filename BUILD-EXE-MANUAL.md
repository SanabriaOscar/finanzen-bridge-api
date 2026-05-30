# Manual — Build `finanzen-bridge.exe` (front + back integrados)

Este documento explica cómo generar el instalador Windows que incluye:

- **Backend** Spring Boot (Java 21) — WebSocket hardware + REST
- **Frontend** Angular — panel admin embebido en el JAR (`/static`)

---

## 1. ¿Necesito Java 21?

| Acción | ¿Java 21? |
|--------|-----------|
| **Compilar** el proyecto (Maven) | **Sí** — JDK 21 en la PC de desarrollo |
| **Ejecutar el JAR** directamente | **Sí** — `java -jar ...` requiere JDK/JRE 21 |
| **Ejecutar el `.exe` instalado** (usuario final) | **No** — el instalador generado por `jpackage` empaqueta un runtime Java dentro de la app |

En resumen: **tú compilas con Java 21**; el cajero/usuario POS solo ejecuta `finanzen-bridge.exe` tras instalar.

---

## 2. Requisitos en la PC de build (Windows)

1. **JDK 21** (no solo JRE) — [Adoptium Temurin 21](https://adoptium.net/) o Oracle JDK 21  
2. **Maven 3.9+** — [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)  
3. **Conexión a internet** — Maven descarga Node v20 automáticamente para compilar Angular (no hace falta instalar Node a mano)

Opcional para desarrollo front sin Maven:

- Node.js 20+ y npm (solo si quieres `npm start` en `frontend/`)

---

## 3. Configurar JAVA_HOME (Windows)

PowerShell (sesión actual):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
```

Debe mostrar **version 21** en ambos.

Verificación permanente: Panel de control → Variables de entorno → `JAVA_HOME` apuntando al JDK 21.

---

## 4. Estructura del proyecto

```
finanzen-bridge-api/
├── backend/          ← Spring Boot (Maven build aquí)
│   ├── pom.xml
│   └── src/
└── frontend/         ← Angular (se compila y copia dentro del JAR)
    ├── package.json
    └── src/
```

---

## 5. Build paso a paso

### Paso A — Abrir terminal en la carpeta backend

```powershell
cd "C:\Users\Oscar Jesus Sanabria\Documents\GitHub\Finnanzen-home-repo\9-finanzen fix temas experienca\finazen-bridge\finanzen-bridge-api\backend"
```

### Paso B — Build completo (front + back + `.exe`)

```powershell
mvn clean package -Pwith-ui,win-exe
```

**Qué hace este comando:**

| Fase | Acción |
|------|--------|
| `-Pwith-ui` | Instala Node 20, ejecuta `npm install` + `npm run build` en `../frontend` |
| | Copia el Angular compilado a `backend/target/classes/static/` |
| `package` | Compila Java 21 y genera el JAR ejecutable |
| `-Pwin-exe` | Ejecuta `jpackage` y crea el instalador Windows |

**Duración estimada:** 5–15 minutos la primera vez (descarga Node, npm packages, Maven deps).

### Paso C — Ubicación del `.exe`

```
backend\dist\finanzen-bridge-1.0.0.exe
```

También se genera el JAR (útil para pruebas):

```
backend\target\finanzen-bridge-backend-1.0.0-SNAPSHOT.jar
```

---

## 6. Probar antes del `.exe` (solo JAR)

Útil para validar que front + back integran bien:

```powershell
cd backend
mvn clean package -Pwith-ui
java -jar target\finanzen-bridge-backend-1.0.0-SNAPSHOT.jar
```

Abrir en el navegador:

| Recurso | URL |
|---------|-----|
| Panel admin Angular | http://127.0.0.1:9095/ |
| API status | http://127.0.0.1:9095/api/bridge/status |
| WebSocket | ws://127.0.0.1:9095/ws?token=finnazen-bridge-local-dev |

Al arrancar, el bridge abre el navegador automáticamente (`open-browser-on-start: true`).

Detener: `Ctrl + C` en la terminal.

---

## 6b. Arrancar el backend en IntelliJ IDEA

1. **File → Open** → carpeta `finanzen-bridge-api/backend` (o el módulo Maven `finanzen-bridge-backend`).
2. **File → Project Structure → Project** → SDK **Java 21** (`C:\Program Files\Java\jdk-21.0.10`).
3. Localice la clase **`com.finnazen.bridge.FinnazenBridgeApplication`**.
4. Clic derecho → **Run 'FinnazenBridgeApplication'** (o Shift+F10).

**Run Configuration recomendada:**

| Campo | Valor |
|-------|--------|
| Main class | `com.finnazen.bridge.FinnazenBridgeApplication` |
| Working directory | `...\finanzen-bridge-api\backend` |
| JRE | 21 |
| Environment (opcional) | `BRIDGE_PRINTER_NAME=XPrinter` |

Tras arrancar, abra **http://127.0.0.1:9095/** — el panel debe mostrar **Conectado** (punto verde).

> Si solo corre `npm start` en `frontend/` (puerto 4300), el panel sigue en **Desconectado** hasta que el backend esté en el puerto **9095**.

---

## 7. Instalar y usar el `.exe` (usuario final)

### Crear el instalador (una sola vez, en tu PC de desarrollo)

```powershell
cd "...\finanzen-bridge-api\backend"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
mvn clean package "-Pwith-ui,win-exe"
```

Salida: `backend\dist\finanzen-bridge-1.0.0.exe` (5–15 min la primera vez).

### Instalar en tu PC (Siguiente → Siguiente → Finalizar)

1. Doble clic en **`finanzen-bridge-1.0.0.exe`**
2. **Siguiente** → elija carpeta (ej. `C:\Program Files\Finnazen Bridge`) → **Siguiente**
3. Marque “crear acceso directo en el escritorio” si aparece → **Instalar**
4. **Finalizar**
5. Abra **Finnazen Bridge** desde el menú Inicio o el escritorio
6. Se abre el navegador en **http://127.0.0.1:9095/** con el panel local
7. Conecte la **XPrinter 80 mm** USB; pruebe **Prueba impresora**

**No necesita instalar Java** en la PC del cajero: el `.exe` trae Java embebido.

**Token por defecto (desarrollo):** `finnazen-bridge-local-dev`

Producción — variable de entorno antes de arrancar:

```powershell
set BRIDGE_PAIRING_TOKEN=mi-token-secreto-largo
```

Y en Angular cloud:

```javascript
localStorage.setItem('finnazen.bridge.pairingToken', 'mi-token-secreto-largo');
```

---

## 8. Solución de problemas

### `JAVA_HOME environment variable is not defined correctly`

- Instala JDK 21 (no JRE solo)
- Configura `JAVA_HOME` y reinicia la terminal

### `jpackage` falla o no se encuentra

- Debes usar **JDK completo**, no JRE
- Verifica: `"$env:JAVA_HOME\bin\jpackage.exe"` existe

### Fallo en `npm install` / Angular build

```powershell
cd ..\frontend
npm install
npm run build
```

Si compila manual, vuelve a `backend` y ejecuta `mvn package -Pwin-exe` (sin `-Pwith-ui` si el dist ya existe).

### SmartScreen bloquea el `.exe`

Normal en builds sin firma de código. Para producción: firmar el instalador con certificado de código.

### `npm ERR! ERR_INVALID_ARG_TYPE` — "file" must be string, Received undefined

Suele ocurrir en **Windows** con **npm 9** durante el `postinstall` de `esbuild` (dependencia de Angular).

**Solución recomendada (manual en `frontend/`):**

```powershell
cd finanzen-bridge-api\frontend
npm install --ignore-scripts
npm run build
```

El proyecto ya incluye `.npmrc` con `ignore-scripts=true` y el perfil Maven `-Pwith-ui` usa `npm install --ignore-scripts`. No necesitas compilar esbuild a mano: Angular usa el binario precompilado `@esbuild/win32-x64`.

**Opcional:** actualizar npm global a 10+ (`npm install -g npm@10`) para evitar el bug en otros proyectos.

---

### Puerto 9095 ocupado

Cambia en `backend/src/main/resources/application.yml` o variable:

```powershell
set SERVER_PORT=9096
```

---

## 9. Comandos resumen

```powershell
# Desarrollo front solo
cd finanzen-bridge-api\frontend
npm install --ignore-scripts
npm start

# JAR integrado (requiere Java 21 instalado)
cd finanzen-bridge-api\backend
mvn clean package -Pwith-ui
java -jar target\finanzen-bridge-backend-1.0.0-SNAPSHOT.jar

# Instalador Windows .exe (requiere JDK 21 solo para compilar)
cd finanzen-bridge-api\backend
mvn clean package -Pwith-ui,win-exe

# Tests unitarios backend
mvn test
```

---

## 10. Flujo visual

```
frontend/ (Angular)
       │
       │  npm run build  (-Pwith-ui)
       ▼
dist/finanzen-bridge-ui/  ──copia──►  backend/target/classes/static/
       │
       │  mvn package
       ▼
finanzen-bridge-backend-1.0.0-SNAPSHOT.jar
       │
       │  jpackage  (-Pwin-exe)
       ▼
target/dist/finnanzen-bridge-1.0.0.exe  ← entregar al usuario POS
```
