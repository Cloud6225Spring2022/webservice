#!/bin/bash

#sudo systemctl stop tomcat.service
sudo systemctl stop application.service
# sudo systemctl stop amazon-cloudwatch-agent.service
#sudo systemctl stop amazon-cloudwatch-agent.service

sudo mv /home/ec2-user/amazon-cloudwatch-config.json /opt/

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/amazon-cloudwatch-config.json -s

#removing previous build ROOT folder
#sudo rm -rf /opt/tomcat/webapps/ROOT

#sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

# cleanup log files
#sudo rm -rf /opt/tomcat/logs/catalina*
#sudo rm -rf /opt/tomcat/logs/*.log
#sudo rm -rf /opt/tomcat/logs/*.txt