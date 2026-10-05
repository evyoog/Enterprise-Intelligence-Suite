FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt requirements-embeddings.txt ./
RUN pip install --no-cache-dir -r requirements.txt
# REQ-PRT-003: build with --build-arg WITH_EMBEDDINGS=true to include the
# sentence-transformers runtime for a real embedding model.
ARG WITH_EMBEDDINGS=false
RUN if [ "$WITH_EMBEDDINGS" = "true" ]; then pip install --no-cache-dir -r requirements-embeddings.txt; fi
COPY app ./app
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
