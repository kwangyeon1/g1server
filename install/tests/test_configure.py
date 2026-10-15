"""Wizard interaction and persistence checks in isolated projects; no model or DB."""
import contextlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

INSTALL = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(INSTALL))
from build_bundle import payload
from configure import Wizard, Cancel, patterns, command
from harness import Harness, quiet


class ConfigureTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='wizard project ')
        self.root = Path(self.temp.name)
        for name, data in payload().items():
            path = self.root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        self.install = self.root / 'install'

    def tearDown(self):
        self.temp.cleanup()

    def wizard(self, answers):
        values = iter(answers)
        return Wizard(self.install, lambda prompt: next(values))

    def test_cancel_leaves_config_docs_and_runtime_untouched(self):
        wizard = self.wizard(['*.py', '', '', '', '', '3', '# New purpose', '.', '', '', '', '3'])
        before = {name: (self.install / name).read_bytes() for name in wizard.original}
        with contextlib.redirect_stdout(io.StringIO()), self.assertRaises(Cancel):
            wizard.run()
        self.assertTrue(all((self.install / name).read_bytes() == value for name, value in before.items()))
        self.assertFalse((self.install / '.runtime').exists())

    def test_save_preserves_unedited_fields_and_backs_up_changed_files(self):
        wizard = self.wizard([])
        wizard.config['commands'] = {'test': ['python', '-m', 'unittest']}
        wizard.docs['project'] += '\nAdded project detail.\n'
        with contextlib.redirect_stdout(io.StringIO()):
            backup = wizard.save()
        self.assertEqual(json.loads((self.install / 'config.json').read_text())['document_globs'], ['install/knowledge/**/*.md'])
        self.assertEqual((backup / 'knowledge/project.md').read_text(encoding='utf-8'), wizard.original['knowledge/project.md'])
        self.assertEqual((self.install / 'knowledge/testing.md').read_text(encoding='utf-8'), wizard.original['knowledge/testing.md'])

    def test_concurrent_edit_prevents_overwrite(self):
        wizard = self.wizard([])
        wizard.docs['project'] = 'wizard edit'
        (self.install / 'knowledge/project.md').write_text('other writer', encoding='utf-8')
        with self.assertRaisesRegex(ValueError, '다른 작업'):
            wizard.save()
        self.assertEqual((self.install / 'knowledge/project.md').read_text(), 'other writer')

    def test_back_discards_partial_step_and_can_return_to_scope(self):
        # Return from the first document to scope, then cancel at preview.
        wizard = self.wizard(['', '', '', '', '', ':back', '*.py', '', '', '', '', '', '', '', '', '3'])
        with contextlib.redirect_stdout(io.StringIO()), self.assertRaises(Cancel):
            wizard.run()
        self.assertEqual(wizard.config['source_globs'], ['*.py'])
        self.assertNotEqual(json.loads((self.install / 'config.json').read_text())['source_globs'], ['*.py'])

    def test_full_flow_saves_review_and_search_without_running_registered_command(self):
        (self.root / 'main.py').write_text('value = 1')
        marker = self.root / 'must-not-run'
        argv = json.dumps([sys.executable, '-c', 'raise SystemExit(99)'])
        answers = ['', '', '', '', argv, '2', '주문 처리 프로젝트 설명', '.', '', '', '', '1',
                   '2', 'main.py와 문서의 목적 및 테스트 범위를 대조함', 'REVIEW', '프로젝트', '2']
        script = self.install / 'configure.py'
        proc = subprocess.run([sys.executable, '-X', 'utf8', str(script)], input='\n'.join(answers) + '\n',
                              capture_output=True, text=True, encoding='utf-8', cwd=self.root)
        self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)
        self.assertIn('검색 상태: FOUND', proc.stdout)
        self.assertTrue((self.install / 'knowledge/source-baseline.json').exists())
        self.assertFalse((self.install / '.runtime/runs').exists())
        self.assertFalse(marker.exists())
        self.assertEqual(json.loads((self.install / 'config.json').read_text())['commands']['test'], json.loads(argv))

    def test_declining_review_keeps_baseline_absent(self):
        (self.root / 'main.py').write_text('value = 1')
        wizard = self.wizard(['', '', '', '', '', '', '', '', '', '1', '3', '', '2'])
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(wizard.run(), 2)
        self.assertFalse((self.install / 'knowledge/source-baseline.json').exists())

    def test_invalid_patterns_and_commands(self):
        for value in ('../outside/*.py', 'C:/files/*.py', '/etc/*'):
            with self.assertRaises(ValueError): patterns(value)
        with self.assertRaises(ValueError): patterns('logs/tmp', directories=True)
        for value in ('"npm test"', '[]', '[1]', '{bad}'):
            with self.assertRaises(ValueError): command(value)
        self.assertEqual(patterns('*.py, *.py, *.ts'), ['*.py', '*.ts'])

    def test_legacy_source_scope_honors_excluded_directory(self):
        (self.root / 'src').mkdir()
        (self.root / 'src/main.py').write_text('value = 1')
        h = Harness(self.root, self.install)
        h.config.pop('source_mode')
        h.config.update(source_globs=['**/*.py'], exclude_dirs=['src', 'install'])
        self.assertEqual(h.sources(), {})


if __name__ == '__main__':
    unittest.main()
