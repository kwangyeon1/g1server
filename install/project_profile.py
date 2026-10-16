"""Stack-neutral source discovery. No dependency installation or command execution."""
import fnmatch
import hashlib
import os
from pathlib import Path

EXCLUDED = {'.git', '.hg', '.svn', '.codex', '.claude', '.venv', 'venv', 'env',
            'node_modules', 'vendor', '__pycache__', '.runtime', '.gradle', '.idea',
            'dist', 'build', 'target', 'bin', 'obj', '.next', '.nuxt', 'coverage'}
PATTERNS = ['*.py', '*.js', '*.jsx', '*.ts', '*.tsx', '*.mjs', '*.cjs', '*.vue', '*.svelte',
            '*.java', '*.kt', '*.kts', '*.gradle', '*.go', '*.rs', '*.cs', '*.fs', '*.vb',
            '*.c', '*.h', '*.cpp', '*.hpp', '*.rb', '*.php', '*.swift', '*.scala', '*.sh',
            '*.ps1', '*.sql', '*.xml', '*.json', '*.toml', '*.yaml', '*.yml', '*.md',
            '*.html', '*.css', '*.scss', '*.csproj', '*.fsproj', '*.sln', '*.dart', '*.ex',
            '*.exs', '*.lua', '*.r', '*.R', 'Makefile', 'Dockerfile', 'Gemfile', 'go.sum',
            'Cargo.lock', 'requirements*.txt', 'CMakeLists.txt', '*.properties']
SECRETS = ['.env*', '*.pem', '*.key', '*.pfx', '*.p12', 'auth.json', 'credentials*',
           'secrets*', '*secret*.json', '*.keystore']


def detect_project(root):
    markers = [('package.json', 'Node/JavaScript'), ('pyproject.toml', 'Python'),
               ('requirements.txt', 'Python'), ('build.gradle*', 'Gradle'), ('pom.xml', 'Maven'),
               ('go.mod', 'Go'), ('Cargo.toml', 'Rust'), ('*.csproj', '.NET'),
               ('*.sln', '.NET'), ('composer.json', 'PHP'), ('Gemfile', 'Ruby'),
               ('CMakeLists.txt', 'C/C++'), ('pubspec.yaml', 'Dart/Flutter')]
    return sorted({label for pattern, label in markers if next(root.glob(pattern), None)}) or ['Generic / empty']


def is_test(name):
    p = Path(name)
    return bool(set(p.parts) & {'test', 'tests', '__tests__'}) or any(
        x in p.name for x in ('test_', '_test.', '.test.', '.spec.', 'Test.'))


def scan_sources(root, install, config):
    found = {}
    excludes = EXCLUDED | set(config.get('exclude_dirs', []))
    patterns = config.get('source_globs', PATTERNS)
    for directory, dirs, files in os.walk(root, followlinks=False):
        base = Path(directory)
        dirs[:] = sorted(d for d in dirs if d not in excludes and not (base / d).is_symlink()
                         and (base / d).resolve() != install.resolve())
        for name in sorted(files):
            p = base / name
            rel = p.relative_to(root).as_posix()
            if p.is_symlink() or name in ('AGENTS.md', 'CLAUDE.md', '.mcp.json'):
                continue
            if any(fnmatch.fnmatch(name.lower(), pat.lower()) for pat in SECRETS):
                continue
            if any(fnmatch.fnmatchcase(rel, pat) or
                   (pat.startswith('**/') and fnmatch.fnmatchcase(rel, pat[3:])) for pat in patterns):
                if p.stat().st_size > config.get('max_source_bytes', 8 * 1024 * 1024):
                    raise ValueError('Source exceeds max_source_bytes; explicitly adjust scope: ' + rel)
                found[rel] = hashlib.sha256(p.read_bytes()).hexdigest()
    return dict(sorted(found.items()))
