"""Check the installed retrieval path without model calls or app events."""
import argparse
import hashlib
import json
import time
import sys
from bridge import Bridge
from store import write_json


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--query', default='프로젝트')
    args = parser.parse_args()
    bridge = Bridge()
    started = time.perf_counter()
    result = bridge.search(args.query)
    items = result.get('results', [])
    valid = bool(items) and all(hashlib.sha256(bridge.safe(bridge.root, item['path']).read_bytes()).hexdigest()
                               == item['sha256'] for item in items)
    elapsed = time.perf_counter() - started
    context = bridge.context({'session_id': 'local-search-check-no-events', 'prompt': args.query})
    ok = result['status'] == 'FOUND' and valid and '문서 검색 상태: FOUND' in context and 'SHA256=' in context
    report = {'status': 'PASS' if ok else 'FAIL', 'search_status': result['status'],
              'query': args.query, 'elapsed_seconds': round(elapsed, 3),
              'paths': [i['path'] for i in items], 'source_hashes_valid': valid,
              'scope': 'Installed retrieval/context only; not live desktop or semantic quality proof',
              'action': None if ok else 'Review knowledge and source drift, then sync; use --query for a project-specific term'}
    write_json(bridge.install / '.runtime/search/verification.json', report)
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if ok else 1


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    raise SystemExit(main())
