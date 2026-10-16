"""Record measured trials and compare A/B/C; never invent missing model metrics."""
import argparse
from collections import defaultdict
import json
import math
from pathlib import Path
import statistics

METRICS = ('missed_callers', 'missing_evidence', 'review_findings', 'input_tokens', 'output_tokens', 'elapsed_seconds')


def validate(record):
    if record.get('condition') not in ('A', 'B', 'C') or not isinstance(record.get('task_id'), str) or not record['task_id'].strip():
        raise ValueError('condition=A/B/C와 task_id가 필요합니다.')
    for name in METRICS:
        value = record.get(name)
        if value is not None and (type(value) not in (int, float) or not math.isfinite(value) or value < 0):
            raise ValueError(name + ': 미측정은 null, 측정값은 0 이상의 수입니다.')
    for name in ('blocked', 'locate_called_when_blocked'):
        if record.get(name) is not None and type(record[name]) is not bool:
            raise ValueError(name + ': true/false/null을 사용하세요.')
    if record.get('locate_called_when_blocked') is not None and record.get('blocked') is not True:
        raise ValueError('막힌 상황이 아니면 locate_called_when_blocked는 null입니다.')
    return {k: record.get(k) for k in ('condition', 'task_id', 'commit', 'model', *METRICS,
                                     'blocked', 'locate_called_when_blocked', 'evidence')}


def summarize(records):
    groups = defaultdict(list)
    for record in records:
        groups[record['condition']].append(validate(record))
    result = {}
    for condition, rows in sorted(groups.items()):
        result[condition] = {'trials': len(rows), 'metrics': {}}
        for metric in METRICS:
            values = [r[metric] for r in rows if r[metric] is not None]
            result[condition]['metrics'][metric] = {'measured': len(values), 'median': statistics.median(values) if values else None}
        blocked = [r for r in rows if r['blocked'] is True and r['locate_called_when_blocked'] is not None]
        result[condition]['locate_when_blocked'] = {'observed': len(blocked),
            'called': sum(r['locate_called_when_blocked'] for r in blocked)}
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=['record', 'summary'])
    parser.add_argument('file', type=Path, help='실측 JSONL 경로')
    parser.add_argument('--input', type=Path, help='한 회차의 측정 JSON')
    args = parser.parse_args()
    if args.command == 'record':
        if not args.input:
            parser.error('record에는 --input이 필요합니다.')
        record = validate(json.loads(args.input.read_text(encoding='utf-8')))
        args.file.parent.mkdir(parents=True, exist_ok=True)
        with args.file.open('a', encoding='utf-8') as stream:
            stream.write(json.dumps(record, ensure_ascii=False) + '\n')
    else:
        records = [json.loads(line) for line in args.file.read_text(encoding='utf-8').splitlines() if line.strip()]
        print(json.dumps(summarize(records), ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
