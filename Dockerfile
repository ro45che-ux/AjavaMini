FROM tomcat:9-jdk17

# Remove default Tomcat apps to serve ours at the root (/)
RUN rm -rf /usr/local/tomcat/webapps/*

# Create ROOT directory
RUN mkdir -p /usr/local/tomcat/webapps/ROOT

# Copy all project files into ROOT
COPY . /usr/local/tomcat/webapps/ROOT

# Set working directory to ROOT
WORKDIR /usr/local/tomcat/webapps/ROOT

# Download PostgreSQL JDBC driver directly into WEB-INF/lib
RUN curl -L -o WEB-INF/lib/postgresql-42.7.3.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar

# Compile Java files using the servlet-api, mysql-connector, and sqlite jars
RUN javac -cp "lib/*:WEB-INF/lib/*" -d WEB-INF/classes *.java

# Clean up source files from the deployed webapp
RUN rm *.java
