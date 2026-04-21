# NBKR Currency Module for Axelor ERP

Модуль для Axelor ERP, который загружает курсы валют с сайта Национального банка Кыргызской Республики и сохраняет их в отдельную сущность.

Источник данных: `https://www.nbkr.kg/XML/daily.xml`

## Что реализовано

- Автоматическая ежедневная загрузка курсов по cron (`09:00`)
- Ручной запуск импорта из интерфейса Axelor
- Upsert-логика (обновление, а не дублирование) по паре `code + rateDate`
- Логирование результата импорта
- Отображение курсов в UI

## Поля сущности CurrencyRate

- `code` (`String`) - код валюты (USD, EUR, RUB и т.д.)
- `name` (`String`) - наименование валюты
- `nominal` (`Integer`) - номинал
- `rate` (`BigDecimal`) - курс по отношению к KGS
- `rateDate` (`LocalDate`) - дата курса

## Структура ключевых файлов

- `src/main/java/com/nbkr/service/NbkrCurrencyRateService.java` - загрузка и обработка XML, upsert
- `src/main/java/com/nbkr/job/NbkrCurrencyRateJob.java` - Quartz job для автоматического запуска
- `src/main/resources/domains/CurrencyRate.xml` - доменная модель Axelor
- `src/main/resources/views/Nbkr.xml` - UI (grid/form, кнопка, меню, action)
- `src/main/resources/data-init/input/meta_schedule.csv` - cron-настройка

## Запуск через Docker

Требования:

- Docker Desktop
- Docker Compose

Команды:

```bash
docker compose up --build -d
```

Проверка:

```bash
docker compose ps
```

Открыть Axelor:

- [http://localhost:8080](http://localhost:8080)

## Где смотреть в UI

- `Administration -> NBKR -> Currency rates`

## Как запустить импорт вручную

Вариант 1:

- `Administration -> NBKR -> Import daily rates`

Вариант 2:

- открыть `Currency rates` и нажать кнопку `Import from NBKR`

## Автоматический запуск

Cron на импорт настраивается в:

- `src/main/resources/data-init/input/meta_schedule.csv`

Текущий cron:

- `0 0 9 * * ?` (ежедневно в 09:00)

## Примечания

- Модуль собран под стек Axelor 5.1.x / Java 8 (через `axelor/aio-erp`).
- Если меню не видно после обновления, выполните hard refresh (`Cmd+Shift+R`) и перезайдите в систему.
