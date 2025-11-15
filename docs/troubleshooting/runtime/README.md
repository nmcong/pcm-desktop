# Runtime Issues - PCM Desktop

This directory contains documentation for runtime and application startup issues.

## 📋 Issue List

### Critical Issues

1. **[JavaFX Application Startup Issue - macOS/Linux](javafx-application-startup-issue.md)** ✅ RESOLVED
   - **Error**: "Missing JavaFX application class com.noteflix.pcm.PCMApplication"
   - **Platform**: macOS, Linux
   - **Root Cause**: Duplicate library JARs in `lib/others/`
   - **Solution**: Remove duplicate libraries and ensure clean dependency management
   - **Date**: 2025-11-15

## 🔍 Common Runtime Issues

### Application Won't Start

**Symptoms**:
- Application exits immediately
- No window appears
- Error messages in console

**Possible Causes**:
1. Duplicate library JARs → [See JavaFX Startup Issue](javafx-application-startup-issue.md)
2. Wrong Java version → Check `java -version`
3. Missing JavaFX libraries → Run `./scripts/setup.sh`
4. Incorrect JAVA_HOME → Set to Java 21 installation

### Application Crashes During Runtime

**Symptoms**:
- Application starts but crashes unexpectedly
- Stack traces in logs
- Native library errors

**Check**:
1. Review `logs/pcm-desktop.log`
2. Check native library compatibility (`.dylib`, `.so`, `.dll`)
3. Verify all required resources are copied to `out/`

### Performance Issues

**Symptoms**:
- Slow startup
- UI lag
- High CPU/memory usage

**Solutions**:
1. Check database size and indexes
2. Review log level (set to INFO instead of DEBUG)
3. Monitor system resources

## 🛠️ Diagnostic Tools

### Check Application Status

```bash
# Check if application is running
ps aux | grep PCMApplication

# Monitor application in real-time
tail -f logs/pcm-desktop.log
```

### Verify Environment

```bash
# Run diagnostic script
./scripts/run.sh --help

# Check libraries
ls -la lib/*/

# Verify compiled classes
find out -name "*.class" | wc -l
```

### Test JavaFX

```bash
# Test JavaFX installation
java --module-path lib/javafx \
     --add-modules javafx.controls \
     -version
```

## 📊 Performance Monitoring

### Startup Time

Normal startup time: **2-5 seconds**

```bash
# Measure startup time
time ./scripts/run.sh
```

If slower than 10 seconds, check:
- Database migrations (should only run once)
- Embedding model loading
- Network connections

### Memory Usage

Typical memory usage: **200-500 MB**

```bash
# Monitor memory
ps aux | grep PCMApplication | awk '{print $6}'
```

## 🔗 Related Documentation

- [Build Issues](../build/README.md)
- [Database Issues](../database/README.md)
- [Setup Guide](../../planning/IMPLEMENTATION_CHECKLIST.md)

---

**Last Updated**: 2025-11-15

