# ai-service

Python / FastAPI service for the platform's shared AI capabilities. Currently a skeleton exposing `GET /health`.

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
