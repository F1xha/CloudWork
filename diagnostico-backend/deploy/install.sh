#!/usr/bin/env bash
set -euo pipefail
if [[ $EUID -ne 0 ]]; then echo "Ejecuta: sudo bash deploy/install.sh"; exit 1; fi
cd "$(dirname "$0")/.."
if [[ ! -f target/diagnostico-backend.jar ]]; then echo "Falta target/diagnostico-backend.jar. Compila o copia el JAR primero."; exit 1; fi
apt-get update
apt-get install -y openjdk-17-jre-headless
id diagnostico >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin diagnostico
install -d -o diagnostico -g diagnostico -m 750 /opt/diagnostico
install -d -o root -g root -m 700 /etc/diagnostico
install -o root -g diagnostico -m 640 target/diagnostico-backend.jar /opt/diagnostico/diagnostico-backend.jar
if [[ ! -f /etc/diagnostico/diagnostico.env ]]; then
 install -o root -g root -m 600 deploy/diagnostico.env.example /etc/diagnostico/diagnostico.env
fi
install -m 644 deploy/diagnostico.service /etc/systemd/system/diagnostico.service
systemctl daemon-reload
printf '%s\n' 'Instalado. Completa sudo nano /etc/diagnostico/diagnostico.env y ejecuta sudo systemctl enable --now diagnostico.'
