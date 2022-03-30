#!/bin/bash

nohup sudo java -jar /home/ubuntu/ROOT*.war server.port=8080 > /home/ubuntu/application-execution.out 2>&1 &
sudo iptables -t nat -L
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8080
# mkdir /home/ubuntu/applicationStart