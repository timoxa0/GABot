# GABot - личный кабинет в виде Discord бота

# Установка

- Настройте Gravit на работу с MySQL, не создавая тригер генерации uuid [Wiki](https://gravitlauncher.com/auth/#%D0%BC%D0%B5%D1%82%D0%BE%D0%B4-mysql).
- Зайдите в [Discord Developer Portal](https://discord.com/developers/applications)
- Создайте новое приложение
- Во вкладке `bot` создайте бота
- Нажмите `Copy` в разделе `token`, чтобы скопировать токен
- Установите Liberica JDK на целевой машине
- Создайте пользователя для бота командой `sudo useradd -m -s /bin/bash authbot`
- Перейдите в него `su - authbot`
- Запустите скрипт установки
```bash
bash <(curl https://raw.githubusercontent.com/timoxa0/GABot/master/setup.sh)
```
- Запустите бота `./start.sh`
- Следуйте инструкциям на экране
- Завершите бота через CTRL+C
- Запустите бота в фоне `./startscreen.sh`
- Добавьте бота на дискорд сервер вашего проекта.
