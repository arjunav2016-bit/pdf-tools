# PPTX Converter Backend

Small conversion service for high-fidelity PowerPoint-to-PDF export.

The Android app should call this service when it needs output that matches PowerPoint more closely than the local Apache POI fallback can provide. The service uses LibreOffice headless, so it must run on a machine or container with LibreOffice installed.

## Run With Docker

```bash
docker build -t pptx-converter .
docker run --rm -p 8080:8080 pptx-converter
```

## Run Locally

Install LibreOffice first, then:

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

On Windows, set `LIBREOFFICE_PATH` if `soffice` is not on `PATH`:

```powershell
$env:LIBREOFFICE_PATH = "C:\Program Files\LibreOffice\program\soffice.exe"
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

## API

Health check:

```bash
curl http://localhost:8080/health
```

Convert:

```bash
curl -F "file=@presentation.pptx" http://localhost:8080/convert/presentation-to-pdf -o presentation.pdf
```

Supported input extensions: `.ppt`, `.pptx`, `.odp`.

