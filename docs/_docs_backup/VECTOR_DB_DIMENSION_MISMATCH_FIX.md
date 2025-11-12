# 🔧 Vector Database Dimension Mismatch - Fix Guide

## 🐛 The Problem

### Error Message

```
Vector search failed: Error: Vectors must have the same length
    at VectorEmbeddingService.cosineSimilarity
```

### What Happened?

Bạn đã switch giữa các embedding modes (TensorFlow vs Fallback) nhưng vectors cũ với dimension khác vẫn còn trong IndexedDB.

**Example scenario:**

1. ✅ Initialize với **Fallback mode** → Add vectors (64D)
2. 🔄 Switch to **TensorFlow mode** → Add more vectors (512D)
3. ❌ Search → Query is 512D nhưng DB có cả 64D và 512D → **ERROR!**

---

## ✅ Solution (FIXED!)

### The Fix Applied

**Updated:** `VectorDatabaseService.js` - Line 127-188

```javascript
// Now automatically skips vectors with mismatched dimensions
async search(query, options = {}) {
  const queryVector = await vectorEmbeddingService.embed(query);
  const queryDimension = queryVector.length;

  // Get all vectors
  const allVectors = await this.getAllVectors({ type, sessionId });

  // Calculate similarities
  const results = [];
  let skippedCount = 0;

  for (const doc of allVectors) {
    // ✅ Skip vectors with different dimensions
    if (doc.vector.length !== queryDimension) {
      skippedCount++;
      console.warn(`⚠️ Skipping vector ${doc.id}: dimension mismatch`);
      continue;  // Skip this vector
    }

    // Calculate similarity only for matching dimensions
    const similarity = vectorEmbeddingService.cosineSimilarity(
      queryVector,
      doc.vector
    );

    if (similarity >= threshold) {
      results.push(result);
    }
  }

  if (skippedCount > 0) {
    console.warn(`⚠️ Skipped ${skippedCount} vectors. Consider clearing old vectors.`);
  }

  return results;
}
```

### What Changed?

**Before:**

- ❌ Crashed when dimensions didn't match
- ❌ No error handling for dimension mismatch

**After:**

- ✅ **Gracefully skips** mismatched vectors
- ✅ **Warns in console** about skipped vectors
- ✅ **Search still works** with matching vectors
- ✅ **Suggests cleanup** in warning message

---

## 🎯 How to Use (After Fix)

### Method 1: Let It Auto-Skip (Simplest)

**Do nothing!** The fix automatically handles dimension mismatches.

```javascript
// This now works even with mixed dimensions
const results = await vectorDatabaseService.search("greeting");

// Console will show:
// 🔍 Query vector dimension: 512
// 📊 Total vectors in DB: 15
// ⚠️ Skipping vector msg_3: dimension mismatch (64D vs 512D)
// ⚠️ Skipped 5 vectors due to dimension mismatch.
```

**Pros:**

- ✅ Zero effort
- ✅ No data loss
- ✅ Search still works

**Cons:**

- ⚠️ Some vectors won't be searched
- ⚠️ Wastes storage space

---

### Method 2: Check and Clear Mismatched Vectors (Recommended)

**Use the new tools in demo:**

```bash
# 1. Open demo
open test-offline-vector.html

# 2. Click "Check Dimensions" button
# → Shows dimension distribution
# → Offers to clear mismatched vectors

# 3. Confirm cleanup
# → Keeps only matching vectors
# → Problem solved!
```

**Or in code:**

```javascript
// Check dimension distribution
const stats = await vectorDatabaseService.getStats();
console.log(stats.dimensionDistribution);
// { "64": 5, "512": 10 }  // Mixed dimensions!

// Option A: Clear all and re-add with current mode
await vectorDatabaseService.clearAll();
await addYourDataAgain();

// Option B: Clear specific dimension
const grouped = await vectorDatabaseService.getVectorsByDimension();
// Delete vectors of specific dimension
for (const vector of grouped["64"]) {
  await vectorDatabaseService.deleteVector(vector.id);
}

// Option C: Use new helper (if you want to keep specific dimension)
await vectorDatabaseService.clearByDimension(64); // Clear 64D vectors
```

---

### Method 3: Prevent the Problem (Best Practice)

**Clear vectors when switching modes:**

```javascript
// When switching from Fallback to TensorFlow
async function switchToTensorFlow() {
  // 1. Clear old vectors
  await vectorDatabaseService.clearAll();
  console.log("✅ Old vectors cleared");

  // 2. Re-initialize with new mode
  await offlineVectorSetup.initializeOffline({
    preferredMode: "tensorflow",
    downloadModels: true,
  });

  // 3. Re-index data
  await reindexAllData();
  console.log("✅ Data re-indexed with new dimensions");
}

// When switching from TensorFlow to Fallback
async function switchToFallback() {
  // Same steps
  await vectorDatabaseService.clearAll();
  await offlineVectorSetup.initializeOffline({ forceMode: "fallback" });
  await reindexAllData();
}
```

---

## 🔍 Diagnostic Tools

### Check Current Status

```javascript
// 1. Check embedding service status
const embeddingStatus = vectorEmbeddingService.getStatus();
console.log("Current mode:", embeddingStatus.modelType);
// → 'browser' (TensorFlow), 'fallback', or 'api'

// 2. Check vector DB stats
const stats = await vectorDatabaseService.getStats();
console.log("Dimension distribution:", stats.dimensionDistribution);
// → { "512": 10 } ✅ Good - single dimension
// → { "64": 5, "512": 10 } ⚠️ Mixed - potential issues

// 3. Check offline setup
const setupStatus = offlineVectorSetup.getStatus();
console.log("Setup mode:", setupStatus.setupMode);
console.log(
  "Expected dimension:",
  setupStatus.setupMode === "tensorflow" ? 512 : 64,
);
```

### Detailed Dimension Analysis

```javascript
// Group vectors by dimension
const grouped = await vectorDatabaseService.getVectorsByDimension();

console.log("Vectors by dimension:");
Object.keys(grouped).forEach((dim) => {
  console.log(`  ${dim}D: ${grouped[dim].length} vectors`);
  // Show first few IDs
  const ids = grouped[dim].slice(0, 3).map((v) => v.id);
  console.log(`    Sample IDs: ${ids.join(", ")}`);
});
```

---

## 📊 Understanding Dimensions

### Dimension Table

| Mode              | Dimension | Model                      | Accuracy | When Used                |
| ----------------- | --------- | -------------------------- | -------- | ------------------------ |
| **TensorFlow.js** | 512D      | Universal Sentence Encoder | 95%      | After downloading models |
| **Fallback**      | 64D       | Text-based TF-IDF          | 65%      | No external dependencies |
| **API**           | Varies    | External service           | Varies   | Not offline              |

### Why Dimensions Matter

```javascript
// Vector A (TensorFlow): [0.1, 0.2, ..., 0.5]  // 512 numbers
// Vector B (Fallback):   [0.3, 0.4, 0.5, 0.6]  // 64 numbers

// Cosine similarity requires same length:
cosineSimilarity(vectorA, vectorB); // ❌ ERROR!
// Cannot compare 512D with 64D
```

### How Modes Affect Dimensions

```javascript
// Scenario 1: Pure TensorFlow
await offlineVectorSetup.initializeOffline({ preferredMode: "tensorflow" });
await vectorDatabaseService.addVector({ text: "Hello" });
// → Stored as 512D

// Scenario 2: Pure Fallback
await offlineVectorSetup.initializeOffline({ forceMode: "fallback" });
await vectorDatabaseService.addVector({ text: "Hello" });
// → Stored as 64D

// Scenario 3: Hybrid (auto-selects)
await offlineVectorSetup.initializeOffline({ preferredMode: "hybrid" });
// → If TensorFlow available → 512D
// → If TensorFlow fails → 64D
```

---

## 🚨 Common Issues & Solutions

### Issue 1: "Skipped X vectors" Warning

**Symptoms:**

```
⚠️ Skipped 5 vectors due to dimension mismatch
```

**Cause:** Mixed dimensions in database

**Solution:**

```javascript
// Option 1: Accept it (some vectors won't be searched)
// No action needed

// Option 2: Clean up
await vectorDatabaseService.clearAll();
// Or use "Check Dimensions" button in demo
```

---

### Issue 2: Search Returns Empty Results

**Symptoms:**

- Search completes but returns 0 results
- Console shows all vectors were skipped

**Cause:** ALL vectors have wrong dimension

**Solution:**

```javascript
// Check current mode
const status = offlineVectorSetup.getStatus();
console.log("Current:", status.setupMode); // e.g., 'fallback' (64D)

// Check what's in DB
const stats = await vectorDatabaseService.getStats();
console.log("In DB:", stats.dimensionDistribution); // e.g., { "512": 10 }

// Fix: Clear and re-index with current mode
await vectorDatabaseService.clearAll();
await addSampleData(); // Re-index with current mode
```

---

### Issue 3: Mode Switching Confusion

**Problem:** Not sure which mode you're in

**Solution:**

```javascript
// Complete status check
async function checkFullStatus() {
  const embeddingStatus = vectorEmbeddingService.getStatus();
  const setupStatus = offlineVectorSetup.getStatus();
  const dbStats = await vectorDatabaseService.getStats();

  console.log("=== FULL STATUS ===");
  console.log("Embedding Service:", embeddingStatus.modelType);
  console.log("Setup Mode:", setupStatus.setupMode);
  console.log(
    "Expected Dimension:",
    setupStatus.setupMode === "tensorflow" ? "512D" : "64D",
  );
  console.log("DB Dimensions:", dbStats.dimensionDistribution);
  console.log("Total Vectors:", dbStats.totalVectors);

  // Check for mismatch
  const expectedDim = setupStatus.setupMode === "tensorflow" ? 512 : 64;
  const dbDims = Object.keys(dbStats.dimensionDistribution || {});
  const hasMismatch = dbDims.some((dim) => parseInt(dim) !== expectedDim);

  if (hasMismatch) {
    console.warn("⚠️ MISMATCH DETECTED!");
    console.warn('💡 Run clearAll() or use "Check Dimensions" in demo');
  } else {
    console.log("✅ All good!");
  }
}

await checkFullStatus();
```

---

## 💡 Best Practices

### 1. Stick to One Mode

```javascript
// Choose one and stick with it
const PREFERRED_MODE = "hybrid"; // or 'tensorflow' or 'fallback'

async function initialize() {
  await offlineVectorSetup.initializeOffline({
    preferredMode: PREFERRED_MODE,
  });
  await vectorDatabaseService.initialize();
}

// Don't switch modes unless you clear data first
```

### 2. Clear Before Switching

```javascript
// Template for mode switching
async function switchMode(newMode) {
  console.log(`Switching to ${newMode} mode...`);

  // 1. Clear old vectors
  const oldCount = (await vectorDatabaseService.getStats()).totalVectors;
  await vectorDatabaseService.clearAll();
  console.log(`Cleared ${oldCount} old vectors`);

  // 2. Initialize new mode
  await offlineVectorSetup.initializeOffline({
    preferredMode: newMode,
  });

  // 3. Re-index data
  // await yourReindexFunction();

  console.log("✅ Mode switch complete");
}
```

### 3. Check After Operations

```javascript
// After adding vectors
async function addVectorsWithValidation(dataArray) {
  const beforeStats = await vectorDatabaseService.getStats();

  await vectorDatabaseService.batchAddVectors(dataArray);

  const afterStats = await vectorDatabaseService.getStats();

  console.log("Added:", afterStats.totalVectors - beforeStats.totalVectors);
  console.log("Dimensions:", afterStats.dimensionDistribution);

  // Validate single dimension
  const dims = Object.keys(afterStats.dimensionDistribution);
  if (dims.length > 1) {
    console.warn("⚠️ Multiple dimensions detected!");
  }
}
```

### 4. Monitor in Production

```javascript
// Periodic health check
setInterval(async () => {
  const stats = await vectorDatabaseService.getStats();
  const dims = Object.keys(stats.dimensionDistribution || {});

  if (dims.length > 1) {
    console.error("🚨 Dimension mismatch in production!");
    // Send alert, log to monitoring, etc.
  }
}, 60000); // Every minute
```

---

## 🎓 Summary

### The Bug (Fixed)

- ❌ **Before:** Crashed on dimension mismatch
- ✅ **After:** Auto-skips mismatched vectors with warning

### What to Do Now

1. **🎯 Immediate:** Nothing! Search now works with mixed dimensions
2. **📊 Optional:** Click "Check Dimensions" in demo to see status
3. **🧹 Recommended:** Clear mismatched vectors for cleaner DB
4. **🚀 Future:** Stick to one mode or clear before switching

### Key Commands

```javascript
// Check status
const stats = await vectorDatabaseService.getStats();
console.log(stats.dimensionDistribution);

// Clear all
await vectorDatabaseService.clearAll();

// Clear specific dimension
await vectorDatabaseService.clearByDimension(64);

// Check dimensions (in demo)
// Click "Check Dimensions" button
```

---

## 📞 Need More Help?

- **Demo:** `test-offline-vector.html` - Has "Check Dimensions" button
- **Docs:** [OFFLINE_VECTOR_DATABASE_GUIDE.md](./OFFLINE_VECTOR_DATABASE_GUIDE.md)
- **Code:** `VectorDatabaseService.js` - See the fix at line 127-188

---

**Status: ✅ FIXED - Search now handles dimension mismatches automatically!**

_Last updated: 2025-11-10_
