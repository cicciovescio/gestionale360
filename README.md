# Meridiana

Gestionale PMI in italiano: vendite, acquisti, magazzino, scadenze, prima nota e fatturazione elettronica.

Stack: **Spring Boot 3.3 / Java 21**, **Angular 19**, **H2** in locale (PostgreSQL opzionale).

## Avvio

Apri due terminali.

### 1. API

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
cd C:\Progetti\Bruno\Gestionale\backend
mvn spring-boot:run
```

API su http://localhost:8080

### 2. Interfaccia

```powershell
cd C:\Progetti\Bruno\Gestionale\frontend
npm start
```

App su http://localhost:4200

### Accesso demo

- Email: `bruno@mposta.it`
- Password: `Bruno2006@`

Altri utenti: `anna@meridiana.local` / `Anna2026@`, `luca@meridiana.local` / `Luca2026@`

## PostgreSQL (opzionale)

```powershell
docker compose up -d
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Cosa c’è dentro

Ciclo attivo (offerta → ordine → DDT → fattura → SDI), ciclo passivo, magazzino, scadenze con incasso, prima nota, planning tecnici, statistiche, anagrafiche e catalogo.

La conferma di un documento è immutabile: si storna o si converte, non si sovrascrive.
