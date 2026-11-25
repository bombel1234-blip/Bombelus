# Mój Dzień 📅✅

Prywatna aplikacja Android do planowania dnia: **cele na dany dzień**, **nawyki**, **podcele**, **realizacje** i **uwagi** — wszystko trzymane lokalnie na telefonie, w pełni offline.

## Funkcje

- **Dziś** – widok wybranego dnia (nawigacja ← →, przycisk „Dziś”):
  - cele dnia z zaznaczaniem realizacji i uwagami,
  - podcele,
  - nawyki zaplanowane na ten dzień tygodnia, odhaczanie jednym kliknięciem,
  - **realizacje** – notatka „jak poszło” do każdego nawyku i dnia,
  - **uwagi** – szybkie notatki na dany dzień,
- **Cele** – wszystkie cele: filtry (wszystkie / aktywne / zrobione), edycja (nazwa, uwagi, data), podcele, usuwanie,
- **Nawyki** – pełna definicja nawyku: nazwa, ikona (emoji), kolor, dni tygodnia, opis/zasada; archiwizacja; statystyki: seria dni z rzędu 🔥 i liczba wykonań; rozwijana lista realizacji,
- **Uwagi** – wszystkie notatki z datami, edycja i przenoszenie na inny dzień.

## Technologia

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) – dane wyłącznie lokalnie na urządzeniu
- MVVM (AndroidViewModel + StateFlow)
- minSdk 26, targetSdk 34

## Budowanie

1. Otwórz folder projektu w **Android Studio** (Koala lub nowsze).
2. Jeśli Android Studio zgłosi brak `gradlew` – jednorazowo uruchom `gradle wrapper --gradle-version 8.7` (wersja jest już ustawiona w `gradle/wrapper/gradle-wrapper.properties`), a Studio samo dociągnie Gradle.
3. Zbuduj i uruchom konfigurację `app` (przycisk ▶) na emulatorze lub telefonie.

Z linii poleceń (Gradle + JDK 17):

```bash
gradle assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Struktura

```
app/src/main/java/com/mojdzien/app/
├── MainActivity.kt        # aktywność główna (Compose)
├── MainViewModel.kt       # logika i stan aplikacji
├── MyDayApp.kt            # Application – dostęp do bazy
├── data/                  # Room: encje, DAO, baza
└── ui/                    # ekrany: Dziś, Cele, Nawyki, Uwagi + wspólne komponenty
```

## Pomysły na kolejne wersje (v2)

- przypomnienia o nawykach (WorkManager / powiadomienia),
- eksport danych (JSON / backup),
- widok tygodnia z podsumowaniem,
- cele długoterminowe powiązane z celami dziennymi.
