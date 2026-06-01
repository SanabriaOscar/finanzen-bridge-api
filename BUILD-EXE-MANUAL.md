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

### Paso C — Ubicación del ejecutable

Con `-Pwin-exe` (imagen portable, **sin WiX**):

```
backend\dist\finanzen-bridge\finanzen-bridge.exe
```

Doble clic en ese `.exe` para arrancar el bridge (no es un asistente de instalación).

Instalador tipo setup (`finanzen-bridge-1.0.0.exe`) — requiere **WiX 3+** en PATH:

```powershell
mvn clean package "-Pwith-ui,win-installer"
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

## 7. Dos tipos de entregable (no confundir)

| Perfil Maven | Archivo que repartes | Experiencia usuario | ¿Aparece en Aplicaciones de Windows? | ¿Desinstalar desde Configuración? |
|--------------|----------------------|---------------------|--------------------------------------|-----------------------------------|
| **`win-exe`** | Carpeta `dist\finanzen-bridge\` (copiar entera) | Doble clic → corre ya | No | No |
| **`win-installer`** | Un solo `dist\finanzen-bridge-1.0.0.exe` | Asistente Siguiente → Instalar | **Sí** | **Sí** |

Para **muchas PCs de cajero** (descargar/copiar → instalar → desinstalar cuando quieran), usa **`win-installer`**.

---

## 7b. Instalador para distribución (Siguiente → Siguiente → Aplicaciones de Windows)

### Paso 1 — Instalar WiX (solo en tu PC de desarrollo, una vez)

1. Descarga **WiX Toolset 3.11 o 3.14** (`.exe`): [releases WiX v3](https://github.com/wixtoolset/wix3/releases)
2. Instálalo con las opciones por defecto.
3. Agrega al **PATH** de Windows (ajusta la versión si es distinta):

   `C:\Program Files (x86)\WiX Toolset v3.14\bin`

4. Cierra y abre **PowerShell** o Git Bash. Verifica:

```powershell
candle.exe -?
light.exe -?
```

Si ambos responden, WiX está listo.

### Paso 2 — Generar el instalador

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd "...\finanzen-bridge-api\backend"

# Si ya compilaste el front: npm run build en ..\frontend
mvn clean package "-Pwith-ui,win-installer"
```

**Archivo para repartir** (USB, correo, descarga):

```
backend\dist\finanzen-bridge-1.0.0.exe
```

Ese **único archivo** es el que copian los cajeros. No envíes la carpeta `finanzen-bridge\` del perfil portable.

### Paso 3 — En cada PC del cajero

1. Doble clic en **`finanzen-bridge-1.0.0.exe`**
2. **Siguiente** → carpeta (ej. `C:\Program Files\Finnazen Bridge`) → **Siguiente**
3. Acceso directo en menú Inicio / escritorio (según opciones del asistente) → **Instalar** → **Finalizar**
4. Abrir **Finnazen Bridge** desde Inicio
5. Navegador en **http://127.0.0.1:9095/** — impresora / lector QR

**Desinstalar:** Configuración → Aplicaciones → buscar **Finnazen** / **finanzen-bridge** → Desinstalar.

**No necesita Java** instalado en la PC del cajero.

**En otras PCs:** solo copia/ejecuta `finanzen-bridge-1.0.0.exe`. **No** hace falta WiX ni JDK en el cajero (solo en tu PC al compilar).

---

## 7c. Báscula → ventas (WebSocket)

1. Bridge instalado y en ejecución (`9095`).
2. En `application.yml` del bridge (o variables):

```yaml
finnazen.bridge.scale.enabled: true
finnazen.bridge.scale.port-name: COM3   # puerto de la báscula en Windows
finnazen.bridge.scale.mock-weight-kg: 0  # solo pruebas sin hardware: ej. 1.5
```

3. Front Finnanzen (`:4200`): formulario venta, producto con modo **WEIGHT**, no marcar “Peso manual”.
4. El front escucha `WEIGHT_CHANGED` y rellena **Peso (gr)** automáticamente.

Eventos: `REQUEST_WEIGHT` (pull) y `WEIGHT_CHANGED` (push cada ~500 ms si el peso cambia).

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

### `Can not find WiX tools (light.exe, candle.exe)`

El perfil **`win-installer`** genera un instalador y necesita [WiX Toolset](https://wixtoolset.org) en el PATH.

Para uso diario en tu PC, usa solo **`win-exe`** (genera `dist\finanzen-bridge\finanzen-bridge.exe` portable, sin WiX).

### `Application destination directory ...\dist\finanzen-bridge already exists`

`jpackage` no sobrescribe una carpeta de un build anterior. El front y el JAR **sí compilaron**; solo falló el último paso.

**Opción A — usar el exe que ya está** (si acabas de generarlo antes):

```
backend\dist\finanzen-bridge\finanzen-bridge.exe
```

**Opción B — regenerar** (Git Bash):

```bash
rm -rf dist/finanzen-bridge
mvn package -Pwin-exe
```

(Sin `-Pwith-ui` si ya corriste `npm run build` en `frontend/`.)

**Opción C — build completo** (el `pom.xml` ya borra `dist/finanzen-bridge` antes de `jpackage`):

```bash
mvn clean package "-Pwith-ui,win-exe"
```

### Fallo en `npm install` / Angular build (`npm run build` exit 1)

Maven a veces **no muestra** el error real de Angular. Haz esto en **PowerShell** (no Git Bash):

```powershell
cd finanzen-bridge-api\frontend
Remove-Item -Recurse -Force node_modules, dist -ErrorAction SilentlyContinue
npm install --ignore-scripts
npm run build
```

Si `npm run build` falla aquí, copia el mensaje completo (líneas `error TS` o `NG`).

Si **sí compila**, genera el `.exe` sin recompilar el front:

```powershell
cd ..\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
mvn clean package -Pwith-ui,win-exe
```

O solo el instalador (UI ya en `frontend\dist`):

```powershell
mvn clean package -Pwin-exe
```

**Git Bash + `npm ERR! Cannot read properties of undefined (reading 'stdin')`:**

Es un bug de **npm 9** en MINGW64, no de Angular. Opciones (cualquiera):

```bash
# 1) Sin npm — recomendado en Git Bash
node ./node_modules/@angular/cli/bin/ng.js build finanzen-bridge-ui --configuration production

# 2) Script Windows
./build.cmd

# 3) Tras actualizar .npmrc (script-shell=cmd.exe), reintenta:
npm run build

# 4) Subir npm a 10+ y reintentar
npm install -g npm@10
npm run build
```

Mejor aún: usa **PowerShell** para `npm run build` y `mvn package`.

**Rutas con espacios** (`Oscar Jesus Sanabria`, `9-finanzen fix temas experienca`): si sigue fallando, prueba clonar/compilar en una ruta corta, por ejemplo `C:\dev\finanzen-bridge`.

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
