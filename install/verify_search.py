"""Exercise the one installed locate engine. No model or synthetic app events."""
import argparse
import json
from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).resolve().parent))
from navigation import Locator


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--query', default='프로젝트')
    args = parser.parse_args()
    try:
        result = Locator().locate(args.query)
    except (ValueError, FileNotFoundError) as exc:
        print(json.dumps({'status': 'UNAVAILABLE', 'note': str(exc)}, ensure_ascii=False))
        return 2
    items = result['results']
    passed = bool(items) and all(not i['stale'] and 'excerpt' not in i and 'body' not in i for i in items)
    print(json.dumps({'status': 'PASS' if passed else 'NO_CURRENT_MATCH', 'result': result,
                      'scope': '실제 locate 호출. 앱 신뢰·모델 선택은 별도 검증.'}, ensure_ascii=False, indent=2))
    return 0 if passed else 2


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    raise SystemExit(main())
