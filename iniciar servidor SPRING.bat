@echo off
title FIFO System Development Launcher

echo --- FIFO System: Iniciando Ambiente de Desenvolvimento ---

echo [1/2] Iniciando Backend (Spring Boot) na Porta 8080...
:: O comando 'start' abre uma nova janela cmd (/k mantém a janela aberta)
:: 'cd backend' navega para a pasta, e 'call mvnw.cmd' executa o wrapper Maven no Windows.
start "FIFO Backend (Spring Boot)" cmd /k "cd backend && call mvnw.cmd spring-boot:run"

echo [2/2] Iniciando Frontend (React/Vite) na Porta 5173...
:: Navega para a pasta 'frontend' e inicia o servidor de desenvolvimento do Vite.
start "FIFO Frontend (React/Vite)" cmd /k "cd frontend && npm run dev"

echo.
echo --- Ambos os servidores foram iniciados. Verifique as novas janelas. ---
echo.