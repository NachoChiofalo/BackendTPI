FROM ghcr.io/project-osrm/osrm-backend:latest

# Intentar instalar clientes HTTP para el healthcheck si el gestor de paquetes existe.
# Soporta Alpine (apk) y Debian/Ubuntu (apt-get). Si no existe ninguno, no falla la build.
USER root
RUN sh -c '\
  if command -v apk >/dev/null 2>&1; then \
    apk add --no-cache curl wget || true; \
  elif command -v apt-get >/dev/null 2>&1; then \
    apt-get update && apt-get install -y --no-install-recommends curl wget || true; \
  else \
    echo "No se encontró gestor de paquetes (apk/apt-get). Se omite la instalación de curl/wget."; \
  fi'

# Mantener usuario por defecto de la imagen base
USER root
