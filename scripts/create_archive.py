#!/usr/bin/env python3
"""
Create multi-part ZIP archives of lib directory.
Each part is max 45MB.
"""
import os
import zipfile
import sys
from pathlib import Path

MAX_SIZE_MB = 45
MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024

def get_all_files(lib_dir):
    """Get all files in lib directory with their sizes."""
    files = []
    for root, dirs, filenames in os.walk(lib_dir):
        for filename in filenames:
            filepath = Path(root) / filename
            rel_path = filepath.relative_to(lib_dir.parent)
            size = filepath.stat().st_size
            files.append({
                'path': filepath,
                'rel_path': str(rel_path),
                'size': size
            })
    return files

def create_archives(lib_dir, output_dir):
    """Create multi-part archives."""
    files = get_all_files(lib_dir)
    
    if not files:
        print("[ERROR] No files found in lib directory!")
        return False
    
    # Sort by size (largest first) for better packing
    files.sort(key=lambda x: x['size'], reverse=True)
    
    total_size = sum(f['size'] for f in files)
    print(f"[INFO] Total lib size: {total_size / (1024 * 1024):.2f} MB")
    print(f"[INFO] Number of files: {len(files)}")
    print()
    
    # Create output directory
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Pack files into multiple archives
    part_num = 1
    current_size = 0
    current_files = []
    
    for file_info in files:
        # Check if adding this file would exceed limit
        if current_size + file_info['size'] > MAX_SIZE_BYTES and current_files:
            # Create archive for current batch
            create_archive_part(current_files, output_dir, part_num, current_size)
            part_num += 1
            current_files = []
            current_size = 0
        
        current_files.append(file_info)
        current_size += file_info['size']
    
    # Create final archive
    if current_files:
        create_archive_part(current_files, output_dir, part_num, current_size)
    
    print()
    print("=" * 50)
    print(f"[SUCCESS] Created {part_num} archive parts")
    print("=" * 50)
    
    return True

def create_archive_part(files, output_dir, part_num, total_size):
    """Create a single archive part."""
    archive_name = f"pcm-libs-part{part_num:02d}.zip"
    archive_path = output_dir / archive_name
    
    print(f"[INFO] Creating {archive_name} ({total_size / (1024 * 1024):.2f} MB, {len(files)} files)...")
    
    with zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for file_info in files:
            zipf.write(file_info['path'], file_info['rel_path'])
    
    actual_size = archive_path.stat().st_size
    print(f"[OK] Created {archive_name} (compressed: {actual_size / (1024 * 1024):.2f} MB)")

def main():
    project_root = Path(__file__).parent.parent
    lib_dir = project_root / "lib"
    output_dir = project_root / "archives"
    
    if not lib_dir.exists():
        print("[ERROR] lib directory not found!")
        print("Please run download-deps script first.")
        return 1
    
    print("[INFO] Analyzing lib directory...")
    
    if not create_archives(lib_dir, output_dir):
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())

