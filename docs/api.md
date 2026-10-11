# ScoreKeeperApp — API Contract v1

Локальное приложение, работает офлайн. Базовый URL: `http://localhost:8000`.

## Общий формат состояния матча

Большинство эндпоинтов возвращают **одно и то же** — полное состояние матча. Структура:

```json
{
  "sport": "volleyball",
  "status": "IN_PROGRESS",
  "players": ["Иванов", "Петров"],
  "sets": [24, 20],
  "sets_won": [1, 0],
  "current_set": 3,
  "server": 0,
  "winner": null
}