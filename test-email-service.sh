#!/bin/bash

# Script para probar el servicio de correos masivos
# Asegúrate de que la aplicación esté ejecutándose en localhost:8080

BASE_URL="http://localhost:8080/api/email"

echo "🚀 Probando Servicio de Correos Masivos"
echo "========================================"

# 1. Health Check
echo "1. Health Check..."
curl -s -X GET "$BASE_URL/health"
echo -e "\n"

# 2. Validar configuración
echo "2. Validando configuración..."
curl -s -X GET "$BASE_URL/validar-configuracion"
echo -e "\n"

# 3. Envío simple
echo "3. Enviando correo simple..."
curl -s -X POST "$BASE_URL/enviar" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarios": ["test@example.com"],
    "asunto": "Prueba desde Script",
    "mensaje": "Este es un mensaje de prueba enviado desde el script de testing.",
    "esHtml": false
  }' | jq '.'
echo -e "\n"

# 4. Envío HTML
echo "4. Enviando correo HTML..."
curl -s -X POST "$BASE_URL/enviar" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarios": ["test@example.com"],
    "asunto": "Prueba HTML",
    "mensaje": "<h1>Título de Prueba</h1><p>Este es un <strong>correo HTML</strong> de prueba.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
    "esHtml": true
  }' | jq '.'
echo -e "\n"

# 5. Envío asíncrono
echo "5. Enviando correo asíncrono..."
curl -s -X POST "$BASE_URL/enviar-async" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarios": ["test1@example.com", "test2@example.com"],
    "asunto": "Prueba Asíncrona",
    "mensaje": "Este correo se procesa de forma asíncrona.",
    "esHtml": false
  }'
echo -e "\n"

echo "✅ Pruebas completadas!"
echo "📝 Revisa los logs de la aplicación para ver el resultado detallado."
echo "⚠️  Para pruebas reales, configura credenciales SMTP válidas en application.properties"