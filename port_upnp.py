import miniupnpc
import time

EXTERNAL_PORTS = [8080, 4200]  # backend | frontend
PROTOCOL = 'TCP'
DESCRIPTION = 'Docker App'

upnp = miniupnpc.UPnP()
upnp.discoverdelay = 200
upnp.discover()
upnp.selectigd()

local_ip = upnp.lanaddr
print(f"Local IP detected: {local_ip}")

for port in EXTERNAL_PORTS:
    upnp.addportmapping(port, PROTOCOL, local_ip, port, DESCRIPTION, '')
    print(f"Port {port} opened!")

try:
    print("Press Ctrl+C to close ports...")
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print("Closing ports...")
    for port in EXTERNAL_PORTS:
        upnp.deleteportmapping(port, PROTOCOL)
        print(f"Port {port} closed!")