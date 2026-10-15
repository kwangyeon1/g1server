"""Portable discovery, packaging, and installation tests. No model/network/business DB."""
import contextlib
import io
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

INSTALL = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(INSTALL))
from harness import Harness, quiet, write_json
from build_bundle import default_config, payload


class PortableTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='portable project 한글 ')
        self.root = Path(self.temp.name)
        for name, data in payload().items():
            path = self.root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        self.install = self.root / 'install'
        self.h = Harness(self.root, self.install)

    def tearDown(self):
        self.temp.cleanup()

    def test_empty_and_multi_stack_discovery_excludes_state(self):
        self.assertEqual(self.h.sources(), {})
        files = ['main.py', 'package.json', 'src/index.ts', 'go.mod', 'Cargo.toml', 'App.csproj',
                 'nested/project/code.kt', 'tests/test_example.py']
        for name in files + ['node_modules/a.js', '.venv/x.py', '.env', 'secrets.json', '.git/x.py']:
            p = self.root / name; p.parent.mkdir(parents=True, exist_ok=True); p.write_text('{}')
        self.assertEqual(set(self.h.sources()), set(files))
        self.assertEqual(quiet(self.h.refresh), 2)
        self.assertIn('tests/test_example.py', (self.install / 'knowledge/generated/test-facts.md').read_text(encoding='utf-8'))
        self.assertEqual(quiet(self.h.accept_review, 'Test fixture navigation reviewed'), 0)
        (self.root / 'main.py').write_text('changed')
        self.assertEqual(quiet(self.h.refresh), 2)

    def test_package_excludes_state_and_project_knowledge(self):
        files = payload()
        for name in files:
            self.assertFalse(any(p in Path(name).parts for p in ('.venv', '.runtime', '__pycache__', 'generated', 'hermes')))
            self.assertNotIn('source-baseline.json', name)
            self.assertNotIn('config.local.json', name)
        self.assertTrue(files['install/README.md'].decode().startswith('# Portable Codex + Hermes harness'))
        self.assertIn('초기 템플릿', files['install/knowledge/project.md'].decode())
        self.assertFalse(json.loads(files['install/config.json'])['commands'])

    def test_python_and_typescript_reach_hermes_review_packet(self):
        sys.path.insert(0, str(INSTALL / 'bridge'))
        from bridge import Bridge
        from unittest.mock import patch
        (self.root / 'main.py').write_text('def project_example(): return 1')
        (self.root / 'app.ts').write_text('export const project_example = 1')
        bridge = Bridge(self.root, self.install)
        bridge.queue.initialize()
        bridge.settings.update(settle_seconds=0, abandoned_turn_seconds=0)
        bridge.queue.enqueue({'session_id': 'fixture', 'turn_id': 'one',
                              'hook_event_name': 'Stop', 'cwd': str(self.root),
                              'prompt': 'project_example'}, bridge.settings)
        job = bridge.queue.claim('fixture-worker', 60, 3)
        directory = bridge.runtime / 'fixture-review'
        directory.mkdir()
        with patch.object(bridge, 'search', return_value={'status': 'NO_MATCH', 'results': []}):
            packet, _ = bridge.packet(job, directory)
        self.assertEqual(set(packet['references']), {'main.py', 'app.ts'})

    def test_full_bootstrap_empty_repeat_preserves_user_guidance_and_hooks(self):
        (self.root / 'AGENTS.md').write_text('User project rules\n', encoding='utf-8')
        write_json(self.root / '.codex/hooks.json', {'hooks': {'Stop': [{'hooks': [
            {'type': 'command', 'command': 'echo user-hook'}]}]}})
        command = [sys.executable, str(self.install / 'bootstrap.py'), '--execute']
        for _ in range(2):
            result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8')
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        agents = (self.root / 'AGENTS.md').read_text(encoding='utf-8')
        self.assertTrue(agents.startswith('User project rules'))
        self.assertEqual(agents.count('<!-- project-harness:start -->'), 1)
        hooks = json.loads((self.root / '.codex/hooks.json').read_text(encoding='utf-8'))
        self.assertIn('echo user-hook', json.dumps(hooks))
        self.assertEqual(len(hooks['hooks']['Stop']), 2)
        python = self.install / '.venv' / ('Scripts/python.exe' if sys.platform == 'win32' else 'bin/python')
        check = subprocess.run([str(python), str(self.install / 'bridge/verify_search.py')], capture_output=True, text=True, encoding='utf-8')
        self.assertEqual(check.returncode, 0, check.stdout + check.stderr)

    def test_existing_node_project_requires_review_and_configured_checks(self):
        (self.root / 'package.json').write_text('{"name":"fixture"}')
        (self.root / 'app.ts').write_text('export const value = 1;')
        command = [sys.executable, str(self.install / 'bootstrap.py'), '--execute']
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8')
        self.assertEqual(result.returncode, 2, result.stdout + result.stderr)
        self.assertFalse((self.install / 'knowledge/source-baseline.json').exists())
        quiet(self.h.accept_review, 'Fixture app and package metadata reviewed')
        quiet(self.h.index)
        self.assertEqual(quiet(self.h.search_docs, '프로젝트', 3), 0)
        with self.assertRaises(ValueError):
            self.h.run_check('test', False, 5)
        self.h.config['commands'] = {'test': [sys.executable, '-c', 'print("fixture check")']}
        self.assertEqual(quiet(self.h.run_check, 'test', True, 10), 0)


if __name__ == '__main__':
    unittest.main()
