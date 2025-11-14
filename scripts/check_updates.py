#!/usr/bin/env python3
"""
Check for newer versions of dependencies in pom.xml.
"""
import os
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path
from urllib.error import URLError
import re

MAVEN_CENTRAL = "https://repo1.maven.org/maven2"

def parse_pom(pom_file):
    """Parse pom.xml and extract dependencies."""
    tree = ET.parse(pom_file)
    root = tree.getroot()
    
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
            
            # Resolve property placeholders
            if version.startswith('${') and version.endswith('}'):
                prop_name = version[2:-1]
                version = properties.get(prop_name, version)
            
            dependencies.append({
                'groupId': group_id,
                'artifactId': artifact_id,
                'version': version
            })
    
    return dependencies

def get_latest_version(group_id, artifact_id):
    """Fetch latest version from Maven Central metadata."""
    try:
        group_path = group_id.replace('.', '/')
        metadata_url = f"{MAVEN_CENTRAL}/{group_path}/{artifact_id}/maven-metadata.xml"
        
        with urllib.request.urlopen(metadata_url, timeout=10) as response:
            content = response.read()
        
        # Parse metadata XML
        root = ET.fromstring(content)
        versioning = root.find('versioning')
        if versioning is not None:
            latest = versioning.find('latest')
            if latest is not None:
                return latest.text
            # Fallback to release
            release = versioning.find('release')
            if release is not None:
                return release.text
        
        return None
    
    except Exception as e:
        return None

def compare_versions(v1, v2):
    """Compare two version strings. Returns -1 if v1 < v2, 0 if equal, 1 if v1 > v2."""
    def normalize(v):
        # Extract numeric parts
        parts = re.findall(r'\d+', v)
        return [int(p) for p in parts]
    
    v1_parts = normalize(v1)
    v2_parts = normalize(v2)
    
    # Pad with zeros
    max_len = max(len(v1_parts), len(v2_parts))
    v1_parts.extend([0] * (max_len - len(v1_parts)))
    v2_parts.extend([0] * (max_len - len(v2_parts)))
    
    for i in range(max_len):
        if v1_parts[i] < v2_parts[i]:
            return -1
        elif v1_parts[i] > v2_parts[i]:
            return 1
    
    return 0

def main():
    project_root = Path(__file__).parent.parent
    pom_file = project_root / "pom.xml"
    
    if not pom_file.exists():
        print("[ERROR] pom.xml not found!")
        return 1
    
    print("[INFO] Parsing pom.xml...")
    dependencies = parse_pom(pom_file)
    
    print(f"[INFO] Checking {len(dependencies)} dependencies for updates...")
    print()
    
    updates_available = []
    up_to_date = []
    check_failed = []
    
    for i, dep in enumerate(dependencies, 1):
        artifact = f"{dep['groupId']}:{dep['artifactId']}"
        current_version = dep['version']
        
        print(f"[{i}/{len(dependencies)}] Checking {artifact}...", end='', flush=True)
        
        latest_version = get_latest_version(dep['groupId'], dep['artifactId'])
        
        if latest_version is None:
            print(" [SKIP] Could not fetch")
            check_failed.append(artifact)
            continue
        
        comparison = compare_versions(current_version, latest_version)
        
        if comparison < 0:
            print(f" [UPDATE] {current_version} -> {latest_version}")
            updates_available.append({
                'artifact': artifact,
                'current': current_version,
                'latest': latest_version
            })
        else:
            print(f" [OK] {current_version}")
            up_to_date.append(artifact)
    
    # Summary
    print()
    print("=" * 70)
    print("                         UPDATE SUMMARY")
    print("=" * 70)
    print()
    
    if updates_available:
        print(f"[UPDATES AVAILABLE] {len(updates_available)} dependencies have newer versions:")
        print()
        for update in updates_available:
            print(f"  {update['artifact']}")
            print(f"    Current: {update['current']}")
            print(f"    Latest:  {update['latest']}")
            print()
    else:
        print("[OK] All dependencies are up to date!")
    
    print(f"[INFO] Up to date: {len(up_to_date)}")
    print(f"[INFO] Updates available: {len(updates_available)}")
    if check_failed:
        print(f"[WARNING] Could not check: {len(check_failed)}")
    
    print("=" * 70)
    
    return 0

if __name__ == "__main__":
    sys.exit(main())

