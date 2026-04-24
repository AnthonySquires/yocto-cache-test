# yocto-cache-test

Minimal C application that prints its git commit (with dirty flag) and compile
timestamp. Designed to test Artifactory premirror source caching behaviour in
Yocto/OE builds.

## Building locally

```bash
cmake -B build
cmake --build build
./build/yocto-cache-test
# Output: yocto-cache-test 277a31e built 2026-04-24 23:00:00
```

## Repository structure

| Path | Purpose |
|------|---------|
| `src/main.c` | Prints commit + compile date |
| `CMakeLists.txt` | Build system; extracts git commit at configure time |
| `recipes-example/yocto-cache-test/yocto-cache-test_git.bb` | BitBake recipe (copy into your layer) |
| `.github/workflows/build-and-test.yml` | CI: build + output format check |
| `.github/workflows/dummy-commit.yml` | Manual workflow to advance HEAD and bump SRCREV |

## Artifactory premirror cache testing workflow

This walks through a full cycle of populating, invalidating, and re-populating
the premirror cache.

### Prerequisites

The Yocto build environment must have the Artifactory premirror class available
with these configuration variables:

| Variable | Description |
|----------|-------------|
| `ARTIFACTORY_PREMIRROR_ENABLED` | Set to `"1"` to enable (default `"0"`) |
| `ARTIFACTORY_PREMIRROR_SERVER` | Artifactory server URL |
| `ARTIFACTORY_PREMIRROR_REPO` | Repository name in Artifactory (default `"source-mirror"`) |
| `ARTIFACTORY_PREMIRROR_UPLOAD` | Set to `"1"` to upload on fetch failure (default `"0"`) |

### Step-by-step

#### 1. Add the recipe to your layer

Copy `recipes-example/yocto-cache-test/yocto-cache-test_git.bb` into your Yocto
layer (e.g. `meta-mylayer/recipes-example/yocto-cache-test/`).

#### 2. Enable premirror with upload

Add to `local.conf`:

```bitbake
ARTIFACTORY_PREMIRROR_ENABLED = "1"
ARTIFACTORY_PREMIRROR_SERVER  = "https://artifactory.example.com"
ARTIFACTORY_PREMIRROR_REPO    = "source-mirror"
ARTIFACTORY_PREMIRROR_UPLOAD  = "1"
```

#### 3. Initial build — populate the cache

```bash
bitbake yocto-cache-test
# or: bitbake yocto-cache-test-native
```

This fetches from GitHub (premirror miss) and uploads the source archive to
Artifactory.

#### 4. Verify cache hit

```bash
bitbake -c cleansstate yocto-cache-test
# Remove the download to force a re-fetch
rm -f tmp/downloads/git2_github.com.AnthonySquires.yocto-cache-test.git*
bitbake yocto-cache-test
```

The build should fetch from the premirror (no GitHub access needed).

#### 5. Advance the upstream commit

Go to **Actions → Dummy Commit → Run workflow** on GitHub. This creates a new
commit and updates the `SRCREV` in the recipe to the previous HEAD.

#### 6. Update the recipe in your layer

Copy the updated `yocto-cache-test_git.bb` from this repo into your layer
(the `SRCREV` now points to the new commit).

#### 7. Test cache miss (upload disabled)

```bitbake
ARTIFACTORY_PREMIRROR_UPLOAD = "0"
```

```bash
bitbake -c cleansstate yocto-cache-test
rm -f tmp/downloads/git2_github.com.AnthonySquires.yocto-cache-test.git*
bitbake yocto-cache-test
```

The premirror won't have an archive for the new SRCREV, so BitBake falls back to
cloning from GitHub. Since upload is disabled, the cache is **not** updated.

#### 8. Re-enable upload and rebuild

```bitbake
ARTIFACTORY_PREMIRROR_UPLOAD = "1"
```

```bash
bitbake -c cleansstate yocto-cache-test
rm -f tmp/downloads/git2_github.com.AnthonySquires.yocto-cache-test.git*
bitbake yocto-cache-test
```

This time the premirror misses again, fetches from GitHub, and **uploads** the
new archive to Artifactory.

#### 9. Confirm the updated cache

```bash
bitbake -c cleansstate yocto-cache-test
rm -f tmp/downloads/git2_github.com.AnthonySquires.yocto-cache-test.git*
bitbake yocto-cache-test
```

Should be a premirror cache hit — confirming the new archive was stored
correctly.
