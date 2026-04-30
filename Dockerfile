FROM tomcat:9-jdk17

# Remove default Tomcat apps to serve ours at the root (/)
RUN rm -rf /usr/local/tomcat/webapps/*

# Create ROOT directory
RUN mkdir -p /usr/local/tomcat/webapps/ROOT

# Copy all project files into ROOT
COPY . /usr/local/tomcat/webapps/ROOT

# Set working directory to ROOT
WORKDIR /usr/local/tomcat/webapps/ROOT

# Compile Java files using the servlet-api and mysql-connector jars
RUN javac -cp "lib/*:WEB-INF/lib/*" -d WEB-INF/classes *.java

# Clean up source files from the deployed webapp
RUN rm *.java
