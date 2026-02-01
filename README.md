# 📱 ClimaHoy

Una app desarrollada en Android que hace uso de la API https://www.weatherapi.com/

## ✨ Características
- Permite conocer el estado del tiempo buscando la ciudad en el buscador de la parte superior
- Permite conocer el estado del tiempo de la ubiación actual del usuario presionando el botón de localización
- Una vez aceptado el permiso de ubicación siempre que se abre la app mostrara el estado del tiempo de la ubicación actual

## 🛠️ Tecnologías
- Kotlin
- Android
- Retrofit
- Hilt

Los modelos fueron generados automaticamente usando los JSON que entrega la api al consumir los servicios

Ciudades: https://api.weatherapi.com/v1/search.json?key=de5553176da64306b86153651221606&q=b

Estado del tiempo: https://api.weatherapi.com/v1/forecast.json?key=de5553176da64306b86153651221606&q=5.063718,-75.513731&days=3

Generador de modelos: https://json2kt.com/
