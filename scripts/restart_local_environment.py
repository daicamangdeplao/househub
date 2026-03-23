import os
import shutil
from pathlib import Path

dir_map = {
    "HOUSEHUB_KNOWLEDGENASE_SOURCE_DIRECTORY": "C:\\workspace\\knowledge-base\\source",
    "HOUSEHUB_KNOWLEDGENASE_TARGET_DIRECTORY": "C:\\workspace\\knowledge-base\\target",
    "HOUSEHUB_KNOWLEDGENASE_ARCHIVE_DIRECTORY": "C:\\workspace\\knowledge-base\\archive",
    "HOUSEHUB_KNOWLEDGENASE_PDF_ARCHIVE_DIRECTORY": "C:\\workspace\\knowledge-base\\pdf_archive",
}

def remove_files(env_var):
    value = os.getenv(env_var)
    if value is not None:
        dir_path = Path(value)
        shutil.rmtree(dir_path)
        dir_path.mkdir()
        return True

    dir_path = Path(dir_map[env_var])
    shutil.rmtree(dir_path)
    dir_path.mkdir()
    return True

if __name__ == "__main__":
    for key in dir_map:
        remove_files(key)
