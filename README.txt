Nano Armor Flight (Fabric 1.20.1)
=================================
Сборка:
  1) Нужен JDK 17.
  2) В папке проекта:   gradle wrapper --gradle-version 8.5     (один раз)
  3) ./gradlew build     (Windows: gradlew.bat build)
  4) Готовый мод: build/libs/nano-armor-flight-1.0.0.jar

Для работы в игре нужны: Fabric Loader, Fabric API, Tech Reborn (+Reborn Core).
Player Ability Lib уже встроена в jar. Старый аддон (quantum) не нужен, но и не мешает.

Если Gradle не находит TechReborn/RebornCore в maven:
  - скачайте jar-ы Tech Reborn и Reborn Core для 1.20.1 с Modrinth,
  - положите в папку libs/
  - в build.gradle замените две строки modCompileOnly на:
        modCompileOnly files("libs/TechReborn-1.20-5.8.9.jar")
        modCompileOnly files("libs/RebornCore-1.20-5.8.9.jar")
    (имена файлов - как у ваших jar-ов)
  - если не хватает классов team.reborn.energy, добавьте
        modCompileOnly "teamreborn:energy:3.0.0"
