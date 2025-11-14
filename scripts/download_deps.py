#!/usr/bin/env python3
"""
Download all dependencies from pom.xml to lib directory.
"""
import os
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path
from urllib.error import URLError

MAVEN_CENTRAL = "https://repo1.maven.org/maven2"

def parse_pom(pom_file):
    """Parse pom.xml and extract dependencies."""
    tree = ET.parse(pom_file)
    root = tree.getroot()
    
    # Handle namespace
    ns = {'mvn': 'http://maven.apache.org/POM/4.0.0'}
    
    # Get properties
    properties = {}
    props = root.find('mvn:properties', ns)
    if props is not None:
        for prop in props:
            key = prop.tag.replace('{' + ns['mvn'] + '}', '')
            properties[key] = prop.text
    
    # Get dependencies
    dependencies = []
    deps = root.find('mvn:dependencies', ns)
    if deps is not None:
        for dep in deps.findall('mvn:dependency', ns):
            group_id = dep.find('mvn:groupId', ns).text
            artifact_id = dep.find('mvn:artifactId', ns).text
            version = dep.find('mvn:version', ns).text
            scope = dep.find('mvn:scope', ns)
            scope = scope.text if scope is not None else 'compile'
            
            # Resolve property placeholders
            if version.startswith('${') and version.endswith('}'):
                prop_name = version[2:-1]
                version = properties.get(prop_name, version)
            
            dependencies.append({
                'groupId': group_id,
                'artifactId': artifact_id,
                'version': version,
                'scope': scope
            })
    
    return dependencies

def get_jar_url(dep):
    """Generate Maven Central URL for JAR."""
    group_path = dep['groupId'].replace('.', '/')
    artifact = dep['artifactId']
    version = dep['version']
    jar_name = f"{artifact}-{version}.jar"
    
    return f"{MAVEN_CENTRAL}/{group_path}/{artifact}/{version}/{jar_name}"

def get_target_dir(dep):
    """Determine target directory based on dependency type."""
    artifact = dep['artifactId']
    group = dep['groupId']
    
    if 'javafx' in artifact:
        return 'javafx'
    elif 'lucene' in artifact or 'onnxruntime' in artifact or 'javaparser' in artifact or 'djl' in artifact or 'tokenizers' in artifact:
        return 'rag'
    elif 'richtextfx' in artifact or 'flowless' in artifact or 'reactfx' in artifact or 'undofx' in artifact or 'wellbehaved' in artifact:
        return 'text-component'
    else:
        return 'others'

def download_file(url, dest_path):
    """Download file with progress indication."""
    try:
        print(f"  Downloading: {dest_path.name}...", end='', flush=True)
        
        # Check if file already exists
        if dest_path.exists():
            print(" [SKIP] Already exists")
            return True
        
        urllib.request.urlretrieve(url, dest_path)
        
        file_size = dest_path.stat().st_size / (1024 * 1024)
        print(f" [OK] {file_size:.2f} MB")
        return True
    
    except URLError as e:
        print(f" [FAILED] {e}")
        return False
    except Exception as e:
        print(f" [ERROR] {e}")
        return False

def main():
    project_root = Path(__file__).parent.parent
    pom_file = project_root / "pom.xml"
    lib_dir = project_root / "lib"
    
    if not pom_file.exists():
        print("[ERROR] pom.xml not found!")
        return 1
    
    print("[INFO] Parsing pom.xml...")
    dependencies = parse_pom(pom_file)
    
    # Filter out 'provided' scope (like Lombok)
    runtime_deps = [d for d in dependencies if d['scope'] != 'provided']
    
    print(f"[INFO] Found {len(runtime_deps)} runtime dependencies")
    print()
    
    # Create lib directories
    for subdir in ['javafx', 'others', 'rag', 'text-component']:
        (lib_dir / subdir).mkdir(parents=True, exist_ok=True)
    
    # Download dependencies
    success_count = 0
    failed_count = 0
    
    for dep in runtime_deps:
        target_dir = get_target_dir(dep)
        jar_url = get_jar_url(dep)
        jar_name = f"{dep['artifactId']}-{dep['version']}.jar"
        dest_path = lib_dir / target_dir / jar_name
        
        if download_file(jar_url, dest_path):
            success_count += 1
        else:
            failed_count += 1
    
    print()
    print("=" * 50)
    print(f"[INFO] Download complete!")
    print(f"[INFO] Success: {success_count}, Failed: {failed_count}")
    print("=" * 50)
    
    if failed_count > 0:
        print()
        print("[WARNING] Some downloads failed. Please check the errors above.")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())

