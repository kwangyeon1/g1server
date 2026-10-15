"""Export an allow-listed clean install/ ZIP. Never copies project/runtime state."""
import hashlib
import json
from pathlib import Path
import zipfile
from project_profile import PATTERNS

HERE = Path(__file__).resolve().parent
SOURCES = ['.gitignore', '.gitattributes', 'harness.py', 'project_profile.py',
           'bootstrap.py', 'bootstrap.ps1', 'bootstrap.sh', 'build_bundle.py',
           'configure.py', 'configure.ps1', 'configure.sh',
           'install-hermes.ps1', 'install-hermes.sh', 'setup.ps1', 'setup.sh',
           'setup-bridge.ps1', 'setup-bridge.sh', 'run.ps1', 'run.sh',
           'bridge.ps1', 'bridge.sh', 'bridge-hook.ps1', 'verify-search.ps1',
           'bridge/bridge.py', 'bridge/store.py', 'bridge/hermes_adapter.py',
           'bridge/status_view.py', 'bridge/review-prompt.txt',
           'gradle/harness.init.gradle', 'sql/inspect-table.sql', 'sql/inspect-table-mcp.sql',
           'tests/test_portable.py', 'tests/test_configure.py']


def default_config():
    return {'source_mode': 'portable', 'guidance_marker': 'project-harness',
            'source_globs': PATTERNS + ['go.mod', '*.txt'], 'exclude_dirs': [],
            'max_source_bytes': 8 * 1024 * 1024,
            'sensitive_source_globs': ['**/application*', '**/*Config*', '**/*config*',
                                       '*config*', '*Config*', '**/.env*'],
            'document_globs': ['install/knowledge/**/*.md'], 'validated_test_classes': [],
            'commands': {}, 'review_targets': ['install/knowledge/' + name + '.md'
                                              for name in ('project', 'testing', 'database', 'decisions')]}


def payload():
    data = {'install/' + name: (HERE / name).read_bytes() for name in SOURCES}
    # Neutral, versioned defaults only: never use the live project's config or knowledge.
    for path in sorted((HERE / 'portable').rglob('*')):
        if path.is_file() and not path.is_symlink() and '__pycache__' not in path.parts:
            rel = path.relative_to(HERE / 'portable').as_posix()
            data['install/' + rel] = path.read_bytes()
            data['install/portable/' + rel] = path.read_bytes()  # Keep rebuild sources.
    data['install/config.json'] = (json.dumps(default_config(), ensure_ascii=False, indent=2) + '\n').encode()
    return data


def build():
    files = payload()
    manifest = {'format_version': 1, 'purpose': 'Clean portable bootstrap; no user/project state',
                'files': {name: hashlib.sha256(body).hexdigest() for name, body in sorted(files.items())}}
    files['install/PACKAGE-MANIFEST.json'] = (json.dumps(manifest, indent=2) + '\n').encode()
    target = HERE / 'dist'
    target.mkdir(exist_ok=True)
    archive = target / 'harness-bootstrap.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as output:
        for name, content in sorted(files.items()):
            output.writestr(name, content)
    (target / 'harness-bootstrap.files.txt').write_text('\n'.join(sorted(files)) + '\n', encoding='utf-8')
    sha = hashlib.sha256(archive.read_bytes()).hexdigest()
    (target / 'harness-bootstrap.sha256').write_text(sha + '  ' + archive.name + '\n', encoding='ascii')
    print(json.dumps({'archive': str(archive), 'files': len(files), 'sha256': sha}, indent=2))
    return archive


if __name__ == '__main__':
    build()
