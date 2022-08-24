# RaccoonRemote Server
RaccoonRemote Server allows you to redirect connections to TCP ports to other addresses according to _rules_
>In comparison to iptables, forwarding targets can be servers without a static/public IP >address available on the Internet.
>In this case the [RaccoonRemote Agent](https://github.com/petya136900/raccoon-remote-agent) application must be installed on the target host and connected to RacconRemote Server.

# Types of rules
- Redirect all connections (For example: connections to *:5555 -> w.x.y.z:3389)
> Note: Useful if you want to implement RDP\SSH access to your local server
- By IP
- By subnet
- By user(Bind by IP)
- SNI of TLS connections(For example: https://onedomain:443 -> a.b.c.d:80, but  https://otherdomain:443 -> w.x.y.z:80 )

## Features
- Embedded H2 Database
- Control is performed through the WEB-interface (login _admin_ password _password_ by default)
- Possibility to forward ports to hosts without public IP addresses if they have an agent installed.
## Requirements
- JDK 8

To view the launch arguments, use
```sh
java -jar raccoonremote-server.jar --help
```
## License

MIT