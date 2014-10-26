Files and their uses

Required
jdk-7u1-linux-x64.tar		to run tomcat
apache-tomcat-7.0.22.tar	to serve web app
apache-ant-1.8.2-bin.tar	to build app
mysql-connector-java-5.1.15-bin.jar	to connect to MySQL

context.xml			the JDBC configuration
logging.properties		turn off url param '' warning
server.xml			configure app to port 80 and 8080
tomcat-users.xml		configure the web gui
web.xml				link the JDBC
tomcat7				allow tomcat to be service start/stop/restart
build.xml			to build the c-mart code

install.sh			install tomcat for c-mart