# ai-service

Python / FastAPI service for the platform's shared AI capabilities.

- `GET /health`
- `GET /embed/info`, `POST /embed` — text embeddings for semantic search ([REQ-PRT-003](../docs/02-requirements/FRD/semantic-search/requirement.md), C70)

## Embeddings
Set in `config/secrets.env` (template `config/secrets.env.example`):

| Variable | Meaning |
|---|---|
| `EMBEDDING_PROVIDER` | `sentence-transformers` (a real model) or `stub` (tests only: word hashing, no understanding of meaning) |
| `EMBEDDING_MODEL` | Model id or local path. Not specified yet; must be multilingual (English + Spanish) with 384 dimensions |
| `EMBEDDING_QUERY_PREFIX`, `EMBEDDING_PASSAGE_PREFIX` | Optional text the model expects before queries / passages |

A real model needs `pip install -r requirements-embeddings.txt` (or `docker build --build-arg WITH_EMBEDDINGS=true`) and network access to download the model the first time. The model loads on a background thread at startup; until it is loaded `/embed` answers 503 and the platform searches by keyword only. After changing the model, use **Rebuild and re-embed** on the admin Search page.

## Run locally
```bash
cd ai-service
python -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## Test
```bash
pytest
```

## Layout
- `app/` — application code (`main.py` is the FastAPI entry point)
- `tests/` — pytest suite
