# Front bridge — arreglar `ng serve` / Babel

## Síntoma

```
Cannot find module '...\node_modules\@babel\helper-define-polyfill-provider\lib\index.js'
```

Causa: `node_modules` incompleto o corrupto. **`npm i` con npm 9 puede decir "up to date" sin instalar los archivos.**

## Solución (Git Bash o PowerShell)

Desde `findexso-bridge-api/frontend`:

```bash
# 1. Detenga ng serve (Ctrl+C)

# 2. Borre node_modules (obligatorio)
rm -rf node_modules

# 3. Instale con npm 10 (no use solo "npm i" si tiene npm 9)
npx npm@10.8.2 install

# 4. Verifique que exista el archivo Babel
test -f node_modules/@babel/helper-define-polyfill-provider/lib/index.js && echo OK || echo FALTA

# 5. Arranque con el script del proyecto (puerto 4300)
npm start
```

**No use** `ng serve` a secas (usa puerto 4200 y a veces otro proyecto). Use **`npm start`**.

## Opcional: subir npm global

```bash
npm install -g npm@10
npm install
npm start
```

## Build producción

```bash
npm run build
```
