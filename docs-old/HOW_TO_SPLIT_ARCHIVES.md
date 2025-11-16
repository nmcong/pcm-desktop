# How to Split Archives on macOS

## 🎯 Tại sao cần split?

- ✅ Upload lên GitHub (giới hạn 100MB per file)
- ✅ Gửi email (giới hạn attachment)
- ✅ Chia sẻ qua Google Drive/Dropbox
- ✅ Backup trên nhiều USB

---

## 🚀 Quick Start - Dùng Scripts

### Chia file

```bash
# Chia thành các phần 50MB
./scripts/split-archive.sh models.zip 50m

# Chia thành các phần 100MB (default)
./scripts/split-archive.sh libs.tar.gz

# Kết quả:
# models.zip.partaa (50MB)
# models.zip.partab (50MB)
# models.zip.partac (< 50MB)
# models.zip.merge.sh (script ghép lại)
```

### Ghép lại

```bash
# Cách 1: Dùng merge script
./models.zip.merge.sh

# Cách 2: Dùng merge-archive.sh
./scripts/merge-archive.sh models.zip.part

# Cách 3: Manual
cat models.zip.part* > models.zip
```

---

## 📋 Methods Comparison

| Method            | Pros                  | Cons             | Recommended        |
|-------------------|-----------------------|------------------|--------------------|
| **split command** | ✅ Built-in, simple    | ⚠️ Manual naming | ✅ Quick splits     |
| **zip -s**        | ✅ Integrated          | ⚠️ Only for zips | For new zips       |
| **tar + split**   | ✅ Compression + split | ⚠️ Two steps     | ✅ Best compression |
| **7-Zip**         | ✅ Cross-platform      | ⚠️ Needs install | For Windows compat |

---

## 🎨 Detailed Methods

### Method 1: split command (Simplest) ⭐

**Chia:**

```bash
split -b 50m file.zip file.zip.part
```

**Kết quả:**

```
file.zip.partaa
file.zip.partab
file.zip.partac
```

**Ghép:**

```bash
cat file.zip.part* > file.zip
```

**Sizes available:**

- `50m` = 50 megabytes
- `100m` = 100 megabytes
- `1g` = 1 gigabyte
- `50k` = 50 kilobytes

---

### Method 2: zip -s (For new archives)

**Tạo và chia:**

```bash
zip -r -s 50m archive.zip folder/
```

**Kết quả:**

```
archive.z01 (50MB)
archive.z02 (50MB)
archive.zip (last part)
```

**Ghép:**

```bash
zip -F archive.zip --out complete.zip
```

---

### Method 3: tar + split (Best compression) ⭐

**Nén và chia:**

```bash
tar czf - folder/ | split -b 50m - folder.tar.gz.part
```

**Ghép và giải nén:**

```bash
cat folder.tar.gz.part* | tar xzf -
```

---

### Method 4: 7-Zip (Cross-platform)

**Install:**

```bash
brew install p7zip
```

**Chia:**

```bash
7z a -v50m archive.7z folder/
```

**Kết quả:**

```
archive.7z.001
archive.7z.002
archive.7z.003
```

**Giải nén:**

```bash
7z x archive.7z.001
```

---

## 💡 Use Cases for PCM Desktop

### Use Case 1: Split RAG Models (80MB → GitHub)

```bash
# Models quá lớn (80MB) cho GitHub
cd data/models

# Chia model thành 2 parts (50MB each)
../../scripts/split-archive.sh all-MiniLM-L6-v2.zip 50m

# Upload cả 2 parts lên GitHub:
# all-MiniLM-L6-v2.zip.partaa (50MB)
# all-MiniLM-L6-v2.zip.partab (30MB)

# User download và ghép:
cat all-MiniLM-L6-v2.zip.part* > all-MiniLM-L6-v2.zip
unzip all-MiniLM-L6-v2.zip
```

### Use Case 2: Split Libraries for Distribution

```bash
# Chia lib/rag thành các phần 50MB
cd lib
tar czf - rag/ | split -b 50m - rag.tar.gz.part

# Distribute:
# rag.tar.gz.partaa
# rag.tar.gz.partab

# User ghép:
cat rag.tar.gz.part* | tar xzf -
```

### Use Case 3: Split Full App Bundle

```bash
# Nén và chia toàn bộ app
tar czf - pcm-desktop/ | split -b 100m - pcm-desktop.tar.gz.part

# User ghép:
cat pcm-desktop.tar.gz.part* | tar xzf -
cd pcm-desktop
./scripts/build.sh
```

---

## 🔧 Automation Examples

### Script 1: Split RAG Models Automatically

```bash
#!/bin/bash
# scripts/package-rag-models.sh

echo "📦 Packaging RAG models..."

cd data/models

for model in */; do
    model_name=$(basename "$model")
    
    if [ -d "$model_name" ]; then
        echo "Packaging: $model_name"
        
        # Zip model
        zip -r "${model_name}.zip" "$model_name"
        
        # Split if > 50MB
        size=$(stat -f%z "${model_name}.zip")
        if [ $size -gt 52428800 ]; then
            echo "  Splitting (size: $((size/1024/1024))MB)..."
            split -b 50m "${model_name}.zip" "${model_name}.zip.part"
            rm "${model_name}.zip"
        fi
    fi
done

echo "✅ Done!"
```

### Script 2: Auto-merge on Setup

```bash
#!/bin/bash
# scripts/setup-models.sh

echo "📥 Setting up models..."

cd data/models

# Find all .partaa files
for part in *.partaa; do
    if [ -f "$part" ]; then
        base=$(echo "$part" | sed 's/.partaa$//')
        echo "Merging: $base"
        
        cat ${base}.part* > "$base"
        unzip "$base"
        
        # Cleanup
        rm ${base}.part* "$base"
    fi
done

echo "✅ Models ready!"
```

---

## 📊 Size Guidelines

### GitHub Limits

- Max file size: **100MB**
- Repository size: **1GB** (recommended)
- Large File Storage (LFS): **2GB** per file

### Recommendations

```bash
# For GitHub without LFS
split -b 95m file.zip file.zip.part

# For GitHub with LFS
split -b 1g file.zip file.zip.part

# For email (Gmail: 25MB)
split -b 20m file.zip file.zip.part

# For USB (FAT32: 4GB limit)
split -b 3g file.zip file.zip.part
```

---

## ✅ Verification

### Check split was successful

```bash
# Original file
ls -lh original.zip

# Split parts
ls -lh original.zip.part*

# Verify total size
du -ch original.zip.part* | tail -1
```

### Test merge

```bash
# Merge to different name
cat original.zip.part* > test.zip

# Compare checksums
md5 original.zip
md5 test.zip

# Should be identical!
```

---

## 🎯 Best Practices

1. **Always keep original**: Don't delete source until verified
2. **Test merge**: Always test merging before distributing
3. **Include instructions**: Add README with merge command
4. **Use checksums**: Include MD5/SHA256 for verification
5. **Consistent naming**: Use .partaa, .partab suffix

---

## 📝 Example README for Split Archive

Create `MERGE_INSTRUCTIONS.md`:

```markdown
# How to Merge Split Archive

This archive has been split into multiple parts for easier distribution.

## Method 1: Automatic (macOS/Linux)
\`\`\`bash
cat models.zip.part* > models.zip
unzip models.zip
\`\`\`

## Method 2: Using Script
\`\`\`bash
./models.zip.merge.sh
\`\`\`

## Method 3: Windows
\`\`\`cmd
copy /b models.zip.part* models.zip
\`\`\`

## Verify
Original size: 82MB
MD5: 1a2b3c4d5e6f7g8h9i0j
\`\`\`

---

## 🛠️ Troubleshooting

### "No such file" when merging
```bash
# Make sure you're in the right directory
ls *.part*

# Use full paths
cat /path/to/file.zip.part* > /path/to/output.zip
```

### "Not a valid archive" after merge

```bash
# Check file sizes
ls -lh file.zip.part*

# Verify all parts present
# Should be: partaa, partab, partac, ...

# Check for corruption
md5 file.zip.partaa
```

### "Permission denied"

```bash
chmod +x merge-script.sh
```

---

## 📚 Quick Reference

```bash
# SPLIT
split -b 50m file.zip file.zip.part           # Split by size
tar czf - dir/ | split -b 50m - dir.tgz.part  # Compress & split
zip -r -s 50m archive.zip folder/             # Zip with split

# MERGE
cat file.part* > file                         # Merge parts
cat dir.tgz.part* | tar xzf -                # Merge & extract
zip -F archive.zip --out complete.zip         # Fix zip split

# VERIFY
md5 file                                      # Checksum
du -ch file.part* | tail -1                  # Total size
file merged.zip                               # Check file type
```

---

## ✅ Summary

**Recommended approach for PCM Desktop:**

```bash
# 1. Split with script
./scripts/split-archive.sh models.zip 50m

# 2. Distribute parts + merge script
# models.zip.partaa
# models.zip.partab
# models.zip.merge.sh

# 3. User merges
./models.zip.merge.sh

# Done! ✅
```

**Benefits:**

- ✅ Simple one-command split
- ✅ Auto-generated merge script
- ✅ Clear file naming
- ✅ Easy verification

Happy splitting! 🎉

