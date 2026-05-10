FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
ENV APP_DB_PATH=/app/data/app.db
ENV AUDIO_DIR=/app/data/audio
ENV GEMMA_BACKEND=mock

EXPOSE 8000
CMD ["python", "backend/app.py"]
