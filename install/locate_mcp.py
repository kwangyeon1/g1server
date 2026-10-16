"""Dependency-free MCP stdio server exposing exactly one tool: locate."""
import json
import sys
from navigation import DESCRIPTION, Locator

PROTOCOLS = ('2024-11-05', '2025-03-26', '2025-06-18')
TOOL = {'name': 'locate', 'description': DESCRIPTION,
        'inputSchema': {'type': 'object', 'properties': {
            'query': {'type': 'string', 'minLength': 1, 'maxLength': 2000},
            'limit': {'type': 'integer', 'minimum': 1, 'maximum': 20, 'default': 8}},
            'required': ['query'], 'additionalProperties': False},
        'annotations': {'readOnlyHint': True, 'destructiveHint': False, 'openWorldHint': False}}


def handle(request, locator):
    if not isinstance(request, dict) or request.get('jsonrpc') != '2.0':
        return {'jsonrpc': '2.0', 'id': None, 'error': {'code': -32600, 'message': 'Invalid Request'}}
    if 'id' not in request:
        return None
    response = {'jsonrpc': '2.0', 'id': request['id']}
    method = request.get('method'); params = request.get('params', {})
    if not isinstance(params, dict):
        return {**response, 'error': {'code': -32602, 'message': 'Invalid params'}}
    if method == 'initialize':
        version = params.get('protocolVersion')
        result = {'protocolVersion': version if version in PROTOCOLS else PROTOCOLS[-1],
                  'capabilities': {'tools': {'listChanged': False}},
                  'serverInfo': {'name': 'project-harness-locate', 'version': '1.0.0'},
                  'instructions': DESCRIPTION}
    elif method == 'ping':
        result = {}
    elif method == 'tools/list':
        result = {'tools': [TOOL]}
    elif method == 'tools/call':
        if params.get('name') != 'locate':
            return {**response, 'error': {'code': -32602, 'message': 'Unknown tool'}}
        try:
            arguments = params.get('arguments', {})
            if not isinstance(arguments, dict) or set(arguments) - {'query', 'limit'} or 'query' not in arguments:
                raise ValueError('locate requires query and optional limit')
            data = locator.locate(**arguments)
            result = {'content': [{'type': 'text', 'text': json.dumps(data, ensure_ascii=False)}], 'isError': False}
        except Exception:
            result = {'content': [{'type': 'text', 'text': 'locate를 사용할 수 없습니다. Git HEAD·제외 설정을 확인하고 grep/원문 읽기를 계속하세요.'}], 'isError': True}
    else:
        return {**response, 'error': {'code': -32601, 'message': 'Method not found'}}
    return {**response, 'result': result}


def main():
    locator = Locator()
    while True:
        line = sys.stdin.buffer.readline(131073)
        if not line:
            break
        if len(line) > 131072:
            # Do not interpret fragments of an oversized request as fresh messages.
            while line and not line.endswith(b'\n'):
                line = sys.stdin.buffer.readline(131073)
            reply = {'jsonrpc': '2.0', 'id': None, 'error': {'code': -32600, 'message': 'Request too large'}}
        else:
            try:
                reply = handle(json.loads(line), locator)
            except (ValueError, TypeError):
                reply = {'jsonrpc': '2.0', 'id': None, 'error': {'code': -32700, 'message': 'Parse error'}}
        if reply is not None:
            print(json.dumps(reply, ensure_ascii=False), flush=True)


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    main()
