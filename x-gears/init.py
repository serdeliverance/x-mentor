import argparse
import redis
from urllib.parse import urlparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-u', '--url', help='Redis URL', type=str, default='redis://127.0.0.1:6379')
    args = parser.parse_args()

    initialized_key = 'check:initialized'

    url = urlparse(args.url)
    conn = redis.Redis(host=url.hostname, port=url.port)
    if not conn.ping():
        raise Exception('Redis unavailable')

    initialized = conn.exists(initialized_key)
    if initialized:
        print('Discovered evidence of a previous initialization - skipping.')
        exit(0)

    print('Loading gear - ', end='')
    with open('gear.py', 'rb') as f:
        gear = f.read()
        res = conn.execute_command('RG.PYEXECUTE', gear)
        print(res)

    print('Flag initialization as done - ', end='')
    print(conn.set(initialized_key, 'done'))