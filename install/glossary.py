"""Small, reviewed glossary only. No repository scan, index, queue or model calls."""
import json
from pathlib import Path
import re
import unicodedata


def normalize(text):
    return unicodedata.normalize('NFKC', text).casefold()


def entries(install):
    path = Path(install) / 'knowledge/glossary.json'
    if not path.is_file() or path.is_symlink() or path.stat().st_size > 131072:
        return []
    data = json.loads(path.read_text(encoding='utf-8'))
    if not isinstance(data, dict) or not isinstance(data.get('entries', []), list):
        raise ValueError('용어 사전 entries는 배열이어야 합니다.')
    return [item for item in data.get('entries', [])[:300]
            if isinstance(item, dict) and isinstance(item.get('term'), str)
            and isinstance(item.get('aliases', []), list)
            and all(isinstance(s, str) for s in item.get('aliases', []))
            and isinstance(item.get('symbols', []), list)]


def matches(install, prompt):
    query = normalize(prompt[:12000])
    found = []
    for item in entries(install):
        if item.get('reviewed') is not True:
            continue
        aliases = [item.get('term', '')] + item.get('aliases', [])
        hit = False
        for alias in aliases:
            term = normalize(alias)
            if not term:
                continue
            # Korean substring matching keeps postpositions: 세관검사장으로.
            if re.search('[가-힣]', term):
                hit = term in query
            else:
                hit = bool(re.search(r'(?<![\w])' + re.escape(term) + r'(?![\w])', query))
            if hit:
                break
        symbols = [s for s in item.get('symbols', [])
                   if isinstance(s, str) and re.fullmatch(r'[\w.*:/-]{1,120}', s)]
        if hit and symbols:
            found.append({**item, 'symbols': symbols[:6]})
        if len(found) == 6:
            break
    return found


def context(install, prompt):
    found = matches(install, prompt)
    if not found:
        return ''
    lines = ['[검토된 용어 별칭: grep의 단서입니다. 의미와 동작은 현재 원문으로 확인하세요.]']
    for item in found:
        term = ' '.join(str(item['term']).split())[:80]
        lines.append(term + ' = ' + ', '.join(item['symbols']))
    return '\n'.join(lines)[:1200]
