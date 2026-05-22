import re

file_path = r"C:\Users\minod\.gemini\antigravity\scratch\pdf-tools\app\src\main\java\com\example\pdftools\data\PdfProcessor.kt"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Find all functions defined inside object PdfProcessor
# We look for suspend fun / fun within the file.
funcs = re.findall(r"(suspend\s+fun\s+\w+|fun\s+\w+)\s*\(", content)
print("Functions in PdfProcessor:")
for fn in funcs:
    print(" -", fn)
