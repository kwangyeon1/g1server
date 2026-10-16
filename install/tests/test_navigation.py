"""Real Git, subprocess hooks/MCP and repeated TUI installation in isolated projects."""
import contextlib
import io
import json
from pathlib import Path
import sqlite3
import subprocess
import sys
import tempfile
import tomllib
import unittest
from unittest.mock import patch

INSTALL = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(INSTALL))
from build_bundle import payload
from navigation import Locator
from glossary_hook import respond
from agent_setup import plan, merge_codex_mcp
from installer import run, review_glossary
from navigation_eval import summarize, validate


class NavigationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='harness 한글 project ')
        self.root = Path(self.temp.name)
        for name, content in payload().items():
            path = self.root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(content)
        self.install = self.root / 'install'
        self.git('init', '-q')
        self.write('src/Port.java', 'class Port {\n // 세관검사장으로 변경\n public void inspect() {\n BODY_SENTINEL();\n }\n}\n')
        self.write('src/Ship.py', 'def departure():\n    """반출 처리"""\n    return 1\n')
        self.commit('initial')

    def tearDown(self):
        self.temp.cleanup()

    def write(self, name, text):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding='utf-8')
        return path

    def git(self, *args):
        return subprocess.run(['git', '-C', str(self.root), *args], check=True, capture_output=True).stdout.decode().strip()

    def commit(self, message):
        self.git('add', 'src')
        self.git('-c', 'user.name=Harness Test', '-c', 'user.email=harness-test@example.invalid', 'commit', '-qm', message)

    def locator(self):
        return Locator(self.root, self.install)

    def glossary(self, reviewed=True):
        value = {'version': 1, 'entries': [{'term': '세관검사장', 'aliases': ['검사 장소'],
            'symbols': ['Port', 'inspect'], 'reviewed': reviewed,
            'evidence': [{'path': 'src/Port.java', 'line': 2}]}]}
        self.write('install/knowledge/glossary.json', json.dumps(value, ensure_ascii=False))

    def test_korean_postposition_locations_only_and_commit(self):
        result = self.locator().locate('세관검사장', 3)
        self.assertEqual(result['status'], 'FOUND')
        self.assertEqual(result['results'][0]['path'], 'src/Port.java')
        self.assertEqual(result['index_commit'], self.git('rev-parse', 'HEAD'))
        self.assertNotIn('BODY_SENTINEL', json.dumps(result))
        self.assertFalse(result['results'][0]['stale'])
        self.assertEqual(set(result['results'][0]), {'path', 'start_line', 'end_line', 'symbol', 'via', 'index_commit', 'stale', 'stale_reason'})

    def test_incremental_modify_rename_delete_and_untracked(self):
        locator = self.locator()
        self.assertEqual(locator.update()['updated_files'], 2)
        self.assertEqual(locator.update()['updated_files'], 0)
        self.write('src/new.py', '# only_untracked_concept')
        self.write('src/Port.java', '// new worktree source')
        item = locator.locate('세관검사장')['results'][0]
        self.assertEqual(item['stale_reason'], 'modified')
        self.assertEqual(locator.locate('only_untracked_concept')['status'], 'NO_MATCH')
        self.git('mv', 'src/Ship.py', 'src/Renamed.py')
        (self.root / 'src/Port.java').unlink()
        self.assertEqual(locator.locate('세관검사장')['results'][0]['stale_reason'], 'deleted')
        self.commit('rename delete add')
        report = locator.update()
        self.assertEqual(report['deleted_files'], 2)
        self.assertEqual(report['updated_files'], 2)
        self.assertEqual(locator.locate('세관검사장')['status'], 'NO_MATCH')
        self.assertEqual(locator.locate('반출')['results'][0]['path'], 'src/Renamed.py')

    def test_crlf_checkout_is_not_stale(self):
        locator = self.locator(); locator.update()
        path = self.root / 'src/Port.java'
        path.write_bytes(path.read_bytes().replace(b'\n', b'\r\n'))
        self.assertFalse(locator.locate('세관검사장')['results'][0]['stale'])

    def test_sensitive_generated_binary_and_symlink_excluded_before_indexing(self):
        for name in ['src/deep/DatabaseConfig.java', 'src/app.yml', 'src/app.yaml', 'src/keys.properties',
                     'src/logs/log.py', 'src/generated/api.py', 'src/.aws/secret.py', 'src/.agents/memory.md']:
            self.write(name, 'CONFIDENTIAL_SENTINEL')
        self.write('src/auto.py', '# @generated do not edit\nCONFIDENTIAL_SENTINEL')
        self.write('src/binary.py', '\x00CONFIDENTIAL_SENTINEL')
        with tempfile.TemporaryDirectory() as outside:
            target = Path(outside) / 'outside.py'; target.write_text('CONFIDENTIAL_SENTINEL')
            (self.root / 'src/linked.py').symlink_to(target)
            self.commit('excluded fixtures')
            locator = self.locator(); locator.update()
            with sqlite3.connect(locator.database) as db:
                indexed = {r[0] for r in db.execute('SELECT path FROM files')}
                self.assertEqual(indexed, {'src/Port.java', 'src/Ship.py'})
                self.assertNotIn('confidential', str(db.execute('SELECT terms FROM chunks').fetchall()).lower())

    def test_policy_change_purges_even_without_new_commit(self):
        locator = self.locator(); locator.update()
        config = json.loads((self.install / 'config.json').read_text())
        config['sensitive_source_globs'].append('Port.java')
        self.write('install/config.json', json.dumps(config))
        self.assertEqual(locator.locate('세관검사장')['status'], 'NO_MATCH')
        with sqlite3.connect(locator.database) as db:
            self.assertNotIn('src/Port.java', [r[0] for r in db.execute('SELECT path FROM files')])

    def test_dirty_symlink_never_returns_former_file(self):
        locator = self.locator(); locator.update()
        path = self.root / 'src/Port.java'; path.unlink()
        path.symlink_to(self.root / 'src/Ship.py')
        self.assertEqual(locator.locate('세관검사장')['status'], 'NO_MATCH')

    def test_glossary_only_hook_draft_no_match_and_cwd(self):
        raw = {'hook_event_name': 'UserPromptSubmit', 'cwd': str(self.root), 'prompt': '세관검사장으로 바꿔줘'}
        self.glossary(False)
        self.assertEqual(respond(raw, self.install), {})
        self.glossary()
        with patch('subprocess.run', side_effect=AssertionError('Hook must not search')):
            result = respond(raw, self.install)
        text = result['hookSpecificOutput']['additionalContext']
        self.assertIn('Port', text)
        self.assertNotIn('BODY_SENTINEL', text)
        self.assertEqual(respond({**raw, 'prompt': 'unrelated'}, self.install), {})
        self.assertEqual(respond({**raw, 'cwd': str(self.root.parent)}, self.install), {})
        self.assertFalse((self.install / '.runtime').exists())
        result = self.locator().locate('검사 장소')
        self.assertIn('glossary', result['results'][0]['via'])

    def test_human_glossary_review_and_decline(self):
        self.glossary(False)
        with contextlib.redirect_stdout(io.StringIO()):
            review_glossary(self.install, lambda _: '')
        self.assertFalse(json.loads((self.install / 'knowledge/glossary.json').read_text())['entries'][0]['reviewed'])
        with contextlib.redirect_stdout(io.StringIO()):
            review_glossary(self.install, lambda _: 'REVIEW')
        self.assertTrue(json.loads((self.install / 'knowledge/glossary.json').read_text())['entries'][0]['reviewed'])
        self.assertTrue(list((self.install / '.runtime/install-backups').glob('glossary.*')))

    def test_tui_cancel_is_read_only(self):
        before = (self.install / 'config.json').read_bytes()
        answers = iter(['3', 'private/**', '2'])
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(run(self.install, lambda _: next(answers)), 0)
        self.assertEqual((self.install / 'config.json').read_bytes(), before)
        self.assertFalse((self.install / '.runtime').exists())

    def test_existing_mcp_collision_fails_before_install(self):
        self.write('.mcp.json', json.dumps({'mcpServers': {'project-harness-locate': {'command': 'user-command'}}}))
        answers = iter(['3', '', '1'])
        with contextlib.redirect_stdout(io.StringIO()), self.assertRaisesRegex(ValueError, '같은 이름'):
            run(self.install, lambda _: next(answers))
        self.assertFalse((self.install / '.runtime').exists())
        self.assertFalse((self.install / '.venv').exists())

    def test_real_tui_install_twice_preserves_clients_and_runs_mcp_and_hook(self):
        self.glossary()
        self.write('AGENTS.md', '사용자 규칙\n')
        self.write('.codex/config.toml', 'model = "user-model"\n[mcp_servers.user]\ncommand="user-command"\n')
        self.write('.mcp.json', '{"mcpServers":{"user":{"command":"user-command"}}}')
        self.write('.claude/settings.json', '{"permissions":{"allow":["Read"]},"hooks":{"Stop":[{"hooks":[{"type":"command","command":"echo user-hook"}]}]}}')
        self.write('.codex/hooks.json', '{"hooks":{"UserPromptSubmit":[{"hooks":[{"type":"command","command":"old hook --bridge-managed"}]}]}}')
        for _ in range(2):
            proc = subprocess.run(['sh', str(self.install / 'bootstrap.sh'), '--tui'],
                input='3\n\n1\n', capture_output=True, text=True, encoding='utf-8', cwd=self.root, timeout=60)
            self.assertEqual(proc.returncode, 2, proc.stdout + proc.stderr)
            self.assertIn('설치 결과: 완료, 프로젝트 지식 검토 대기', proc.stdout)
        agents = (self.root / 'AGENTS.md').read_text()
        self.assertTrue(agents.startswith('사용자 규칙'))
        self.assertEqual(agents.count('<!-- project-harness:start -->'), 1)
        self.assertNotIn('{{PROJECT_MAP}}', agents)
        settings = json.loads((self.root / '.claude/settings.json').read_text())
        self.assertEqual(settings['permissions'], {'allow': ['Read']})
        self.assertEqual(len(settings['hooks']['Stop']), 1)
        self.assertNotIn('Interrupt', settings['hooks'])
        self.assertNotIn('SessionStart', settings['hooks'])
        self.assertFalse((self.install / '.runtime/bridge').exists())
        self.assertFalse(list(self.install.rglob('queue.sqlite*')))
        codex_hooks = json.loads((self.root / '.codex/hooks.json').read_text())['hooks']
        self.assertEqual(set(codex_hooks), {'UserPromptSubmit'})
        self.assertEqual(len(settings['hooks']['UserPromptSubmit']), 1)
        codex = tomllib.loads((self.root / '.codex/config.toml').read_text())
        self.assertEqual(codex['model'], 'user-model')
        self.assertIn('user', codex['mcp_servers'])
        self.assertIn('user', json.loads((self.root / '.mcp.json').read_text())['mcpServers'])
        raw = {'hook_event_name': 'UserPromptSubmit', 'cwd': str(self.root), 'prompt': '세관검사장으로'}
        command = settings['hooks']['UserPromptSubmit'][0]['hooks'][0]['command']
        proc = subprocess.run(command, shell=True, input=json.dumps(raw), text=True, capture_output=True, encoding='utf-8')
        self.assertEqual(proc.returncode, 0, proc.stderr)
        self.assertIn('Port', json.loads(proc.stdout)['hookSpecificOutput']['additionalContext'])
        server = codex['mcp_servers']['project-harness-locate']
        messages = [
            {'jsonrpc': '2.0', 'id': 1, 'method': 'initialize', 'params': {'protocolVersion': '2025-06-18', 'capabilities': {}, 'clientInfo': {'name': 'test', 'version': '1'}}},
            {'jsonrpc': '2.0', 'method': 'notifications/initialized'},
            {'jsonrpc': '2.0', 'id': 2, 'method': 'tools/list'},
            {'jsonrpc': '2.0', 'id': 3, 'method': 'tools/call', 'params': {'name': 'locate', 'arguments': {'query': '세관검사장'}}},
            {'jsonrpc': '2.0', 'id': 4, 'method': 'tools/call', 'params': {'name': 'locate', 'arguments': {'query': 'x', 'limit': 0}}},
            {'jsonrpc': '2.0', 'id': 5, 'method': 'missing'}]
        proc = subprocess.run([server['command'], *server['args']], input='\n'.join(json.dumps(m) for m in messages)+'\n',
            capture_output=True, text=True, encoding='utf-8', cwd=self.root, timeout=30)
        self.assertEqual(proc.returncode, 0, proc.stderr)
        replies = [json.loads(line) for line in proc.stdout.splitlines()]
        self.assertEqual(len(replies), 5)
        self.assertEqual([t['name'] for t in replies[1]['result']['tools']], ['locate'])
        result = json.loads(replies[2]['result']['content'][0]['text'])
        self.assertEqual(result['results'][0]['path'], 'src/Port.java')
        self.assertNotIn('BODY_SENTINEL', proc.stdout)
        self.assertTrue(replies[3]['result']['isError'])
        self.assertEqual(replies[4]['error']['code'], -32601)

    def test_disabled_conditions_do_not_create_index_or_hook_context(self):
        self.glossary()
        config = json.loads((self.install / 'config.json').read_text())
        config['navigation'].update(locate_enabled=False, glossary_hook_enabled=False)
        self.write('install/config.json', json.dumps(config))
        self.assertEqual(self.locator().locate('세관검사장')['status'], 'DISABLED')
        self.assertEqual(respond({'cwd': str(self.root), 'hook_event_name': 'UserPromptSubmit', 'prompt': '세관검사장'}, self.install), {})
        self.assertFalse((self.install / '.runtime').exists())

    def test_evaluation_keeps_unmeasured_distinct_from_zero(self):
        rows = [{'condition': 'C', 'task_id': 'one', 'review_findings': None, 'blocked': True, 'locate_called_when_blocked': True},
                {'condition': 'C', 'task_id': 'two', 'review_findings': 0}]
        result = summarize(rows)['C']
        self.assertEqual(result['metrics']['review_findings'], {'measured': 1, 'median': 0})
        self.assertEqual(result['locate_when_blocked'], {'observed': 1, 'called': 1})
        with self.assertRaises(ValueError):
            validate({'condition': 'A', 'task_id': 'bad', 'input_tokens': -1})

    def test_concurrent_clients_share_one_consistent_index(self):
        from concurrent.futures import ThreadPoolExecutor
        with ThreadPoolExecutor(max_workers=2) as pool:
            results = list(pool.map(lambda _: self.locator().locate('세관검사장'), range(2)))
        self.assertTrue(all(r['status'] == 'FOUND' for r in results))
        self.assertEqual(results[0]['index_commit'], results[1]['index_commit'])
        self.assertEqual(self.locator().status()['status'], 'CURRENT')

    def test_guidance_command_updates_both_clients_from_one_template(self):
        self.write('install/knowledge/project-map.md', '## 실측 지도\nsrc/Port.java\n')
        result = subprocess.run([sys.executable, str(self.install / 'harness.py'), 'install-guidance', '--execute'],
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        for name in ['AGENTS.md', 'CLAUDE.md']:
            self.assertIn('실측 지도', (self.root / name).read_text())

    def test_reinstall_removes_all_old_managed_hooks_preserving_user_hooks_and_history(self):
        events = ('SessionStart', 'UserPromptSubmit', 'PostToolUse', 'PreCompact', 'Stop', 'Interrupt', 'SessionEnd')
        old = {'hooks': {event: [{'matcher': '.*', 'hooks': [
            {'type': 'command', 'command': 'old-review --bridge-managed'},
            {'type': 'command', 'command': 'echo user-' + event}]}] for event in events}}
        for relative in ['.codex/hooks.json', '.claude/settings.json']:
            self.write(relative, json.dumps(old))
        history = self.write('install/.runtime/bridge/reviews/old-report.json', '{"history":true}')
        proc = subprocess.run([sys.executable, str(self.install / 'bootstrap.py'), '--execute'],
            capture_output=True, text=True, encoding='utf-8', cwd=self.root, timeout=60)
        self.assertEqual(proc.returncode, 2, proc.stdout + proc.stderr)
        for relative in ['.codex/hooks.json', '.claude/settings.json']:
            data = json.loads((self.root / relative).read_text())
            self.assertNotIn('--bridge-managed', json.dumps(data))
            for event in events:
                self.assertIn('echo user-' + event, json.dumps(data['hooks'][event]))
            commands = [h['command'] for groups in data['hooks'].values() for group in groups for h in group['hooks']]
            self.assertEqual(sum('--glossary-managed' in c for c in commands), 1)
        self.assertEqual(history.read_text(), '{"history":true}')
        self.assertFalse(list(self.install.rglob('queue.sqlite*')))

    def test_plan_has_no_review_installation(self):
        proc = subprocess.run([sys.executable, str(self.install / 'bootstrap.py'), '--plan'],
                              capture_output=True, text=True, encoding='utf-8')
        self.assertEqual(proc.returncode, 0, proc.stderr)
        plan_text = proc.stdout.casefold()
        for removed in ('hermes', 'bridge', 'queue', 'worker'):
            self.assertNotIn(removed, plan_text)
        self.assertFalse((self.install / '.runtime').exists())


if __name__ == '__main__':
    unittest.main()
