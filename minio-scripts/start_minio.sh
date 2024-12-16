#!/bin/bash

DATA_DIR="./minio_data"
if [ ! -d "$DATA_DIR" ]; then
    mkdir -p "$DATA_DIR"
    echo "Создана директория для данных: $DATA_DIR"
fi

echo "Запуск MinIO на порту 9000 и консоль на порту 9001..."
./minio server "$DATA_DIR" --console-address ":9001" &

sleep 2
if pgrep -f "minio server" > /dev/null; then
    echo "MinIO успешно запущен!"
    echo "Консоль доступна по адресу: http://localhost:9001"
else
    echo "Ошибка при запуске MinIO"
fi
