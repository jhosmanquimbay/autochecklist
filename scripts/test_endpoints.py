import re
import requests

BASE = 'http://localhost:8081'

s = requests.Session()

# Get login page and extract CSRF token
r = s.get(f'{BASE}/login', timeout=10)
match = re.search(r'name="_csrf" value="([^"]+)"', r.text)
if not match:
    raise SystemExit('CSRF token not found in login page')
csrf = match.group(1)

print('CSRF token:', csrf)

# Ensure admin user exists (setup endpoint)
setup_page = s.get(f'{BASE}/setup', timeout=10)
setup_match = re.search(r'name="_csrf" value="([^\"]+)"', setup_page.text)
if not setup_match:
    raise SystemExit('CSRF token not found on setup page')
setup_csrf = setup_match.group(1)

setup = s.post(f'{BASE}/setup/create-admin', data={'_csrf': setup_csrf}, timeout=10)
print('Setup create-admin status:', setup.status_code)

# Reset admin password (debug endpoint)
reset = s.post(f'{BASE}/setup/debug/reset-password', data={'correo': 'admin@test.com', 'password': 'admin123', '_csrf': setup_csrf}, timeout=10)
print('Reset password status:', reset.status_code)
print('Reset password response body:', reset.text[:1000])

# Refresh login CSRF token
r = s.get(f'{BASE}/login', timeout=10)
match = re.search(r'name="_csrf" value="([^\"]+)"', r.text)
if not match:
    raise SystemExit('CSRF token not found on login page')
csrf = match.group(1)

# Login (use password from DB: password123)
login = s.post(f'{BASE}/login', data={'username': 'admin@test.com', 'password': 'admin123', '_csrf': csrf}, allow_redirects=True, timeout=10)
print('Login status:', login.status_code, 'Final URL:', login.url)

# Call test send direct
resp = s.get(f'{BASE}/correo/test-send-direct/20/1', timeout=20)
print('\n/test-send-direct response (status', resp.status_code, '):')
print(resp.text)

# Call sync inbox
resp2 = s.post(f'{BASE}/correo/api/sync-inbox', timeout=20)
print('\n/api/sync-inbox response (status', resp2.status_code, '):')
print(resp2.text)

# Call debug external
resp3 = s.get(f'{BASE}/correo/debug/external?email=autochecklistoficial@gmail.com', timeout=20)
print('\n/debug/external response (status', resp3.status_code, '):')
print(resp3.text)
