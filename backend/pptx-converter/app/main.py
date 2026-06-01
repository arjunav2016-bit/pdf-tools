from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import uuid
from pathlib import Path

from fastapi import BackgroundTasks, FastAPI, File, HTTPException, UploadFile
from fastapi.responses import FileResponse


app = FastAPI(title="PPTX Converter Backend", version="1.0.0")

SUPPORTED_EXTENSIONS = {".ppt", ".pptx", ".odp"}
MAX_UPLOAD_BYTES = int(os.getenv("MAX_UPLOAD_BYTES", str(50 * 1024 * 1024)))
CONVERSION_TIMEOUT_SECONDS = int(os.getenv("CONVERSION_TIMEOUT_SECONDS", "120"))
LIBREOFFICE_PATH = os.getenv("LIBREOFFICE_PATH", "soffice")


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "ok": True,
        "libreoffice": LIBREOFFICE_PATH,
        "supported_extensions": sorted(SUPPORTED_EXTENSIONS),
        "max_upload_bytes": MAX_UPLOAD_BYTES,
    }


@app.post("/convert/presentation-to-pdf")
async def convert_presentation_to_pdf(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...),
) -> FileResponse:
    original_name = Path(file.filename or "presentation.pptx").name
    extension = Path(original_name).suffix.lower()
    if extension not in SUPPORTED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported file type '{extension}'. Use ppt, pptx, or odp.",
        )

    work_dir = Path(tempfile.mkdtemp(prefix="pptx_converter_"))
    input_path = work_dir / f"input{extension}"
    output_dir = work_dir / "out"
    profile_dir = work_dir / "lo-profile"
    output_dir.mkdir()
    profile_dir.mkdir()

    try:
        await save_upload(file, input_path)
        convert_with_libreoffice(input_path, output_dir, profile_dir)

        pdf_path = output_dir / "input.pdf"
        if not pdf_path.exists():
            candidates = list(output_dir.glob("*.pdf"))
            if not candidates:
                raise HTTPException(status_code=500, detail="LibreOffice did not produce a PDF.")
            pdf_path = candidates[0]

        background_tasks.add_task(shutil.rmtree, work_dir, ignore_errors=True)
        download_name = f"{Path(original_name).stem}.pdf"
        return FileResponse(
            path=pdf_path,
            media_type="application/pdf",
            filename=download_name,
            background=background_tasks,
        )
    except HTTPException:
        shutil.rmtree(work_dir, ignore_errors=True)
        raise
    except subprocess.TimeoutExpired:
        shutil.rmtree(work_dir, ignore_errors=True)
        raise HTTPException(status_code=504, detail="PowerPoint conversion timed out.")
    except FileNotFoundError:
        shutil.rmtree(work_dir, ignore_errors=True)
        raise HTTPException(
            status_code=500,
            detail="LibreOffice was not found. Set LIBREOFFICE_PATH or install LibreOffice.",
        )
    except Exception as exc:
        shutil.rmtree(work_dir, ignore_errors=True)
        raise HTTPException(status_code=500, detail=f"Conversion failed: {exc}") from exc


async def save_upload(upload: UploadFile, destination: Path) -> None:
    total = 0
    with destination.open("wb") as output:
        while True:
            chunk = await upload.read(1024 * 1024)
            if not chunk:
                break
            total += len(chunk)
            if total > MAX_UPLOAD_BYTES:
                raise HTTPException(status_code=413, detail="Uploaded file is too large.")
            output.write(chunk)

    if total == 0:
        raise HTTPException(status_code=400, detail="Uploaded file is empty.")


def convert_with_libreoffice(input_path: Path, output_dir: Path, profile_dir: Path) -> None:
    profile_uri = profile_dir.resolve().as_uri()
    command = [
        LIBREOFFICE_PATH,
        "--headless",
        "--nologo",
        "--nofirststartwizard",
        "--norestore",
        f"-env:UserInstallation={profile_uri}",
        "--convert-to",
        "pdf",
        "--outdir",
        str(output_dir),
        str(input_path),
    ]

    result = subprocess.run(
        command,
        cwd=str(input_path.parent),
        capture_output=True,
        text=True,
        timeout=CONVERSION_TIMEOUT_SECONDS,
        check=False,
    )

    if result.returncode != 0:
        message = (result.stderr or result.stdout or "LibreOffice conversion failed.").strip()
        raise RuntimeError(message)
