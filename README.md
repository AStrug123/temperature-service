# Temperature Service

This repository contains a Spring Boot application that is containerized using Docker with a lightweight Alpine Linux image. The Docker build process uses a multi-stage build to ensure a small final image size, and the application runs as a non-root user inside the container for security.

# Requirements

- **Java 17**
- **Spring Boot 3.3.4**
- **Maven 3.6.0 or higher**
- **Docker** (for containerized deployment)
- **jenv** (optional for Java version management)

## Assumptions

1. **CSV File Size**:
  - The CSV file is expected to be at least **3GB in size**. This is important to ensure the application is tested with large datasets, which require efficient processing.

2. **File Format**:
  - The file represents temperature data in the following format:
    ```
    city;yyyy-mm-dd HH:mm:ss.SSS;temp
    ```
    - **city**: Name of the city where the temperature is measured.
    - **yyyy-mm-dd HH:mm:ss.SSS**: Timestamp in the format of year-month-day hour:minute:second.millisecond.
    - **temp**: Temperature value as a decimal number.

3. **Dynamic File Changes**:
  - The content of the CSV file may change while the application is running. The application should be able to handle updates to the file dynamically without crashing or requiring a restart.

4. **Example Source File**:
  - An example file (`example_file.csv`) is located in the root directory and contains temperature data for testing purposes.

5. **Replace from the different place:**
   - If you want to replace a file with a different place, replace path in the `run.sh` script
   
   from:
   - "$(pwd)/example_file.csv:/app/example_file.csv"
   
   to:
   - "yourPath/example_file.csv:/app/example_file.csv"
   - 
# Getting Started

## 1. Set Up Java 17 with jenv (Optional but Recommended)

If you're using jenv to manage Java versions, follow these steps to switch to Java 17:

```bash
jenv add /path/to/java17
jenv local 17
```
Verify that Java 17 is being used:

```bash
java -version
```

You should see something like:
```bash
openjdk version "17"
OpenJDK Runtime Environment (build 17)
```

## 2. Grant Permissions to the mvnw and run.sh Scripts

Before running the Maven Wrapper (mvnw) and run.sh scripts, you need to make sure that they have the necessary execution permissions.

**Grant Permission to** mvnw:
```bash
chmod +x mvnw
```

This allows the Maven Wrapper (mvnw) script to be executed.

**Grant Permission to** run.sh:
```bash
chmod +x run.sh
```

This ensures the run.sh script is executable and can be used to build and run the application.

## 3. Build the project

```bash
mvn clean install
```

## 4. Running the Application
To run the application, use the following Maven command:

### Running with maven

You can run the application directly using Maven:

```bash
mvn spring-boot:run
```
### Running the Generated JAR

Alternatively, you can run the generated JAR file:

```bash
java -jar target/temperature-service-0.0.1-SNAPSHOT.jar
```

### Running the Application in Docker (Recommended)

To run the application in Docker, use the provided run.sh script. This script will:
- Run tests
- Build the Docker image
- Run the application inside a Docker container

Steps:
- Ensure Docker is installed and running.
- Run the script:

```bash
./run.sh
```

This script will build the Docker image and expose the application on port 8080.


### Running Tests

To run the tests, use the following Maven command:

```bash
mvn test
```

## API Endpoints

### Allocate Rooms

- **URL:** `/v1/temperatures/{city}`
- **Method:** `GET`
- **Content-Type:** `application/json`
  - **Response Body:**
    ```json
      [
          {
          "year": 2018,
          "averageTemperature": 13.5
          },
          {
          "year": 2019,
          "averageTemperature": 13.8
          },
          {
          "year": 2020,
          "averageTemperature": 16.1
          },
          {
          "year": 2021,
          "averageTemperature": 15.6
          },
          {
          "year": 2022,
          "averageTemperature": 14.7
          },
          {
          "year": 2023,
          "averageTemperature": 15.5
          }
      ]
    ```
## Docker

### Dockerfile

The project includes a multi-stage `Dockerfile` that:

- Downloads dependencies
- Builds the application
- Runs the application in a minimal Java runtime container.

### run.sh Script

The `run.sh` script automates the process of:

1. Running Maven tests.
2. Building the Docker image.
3. Running the application in a Docker container.

To use this script, simply run:

```bash
./run.sh
```
### Exposing Ports
By default, the application runs on port 8080 inside the Docker container. This port is mapped to your local machine, so you can access the application at http://localhost:8080.

## Configuration

### Project Structure

- **src/main/java**: Contains the main application code
- **src/test/java**: Contains the test code
- **src/main/resources**: Contains the application properties
- **Dockerfile**: Contains the Docker configuration for building and running the application
- **run.sh**: Bash script to build and run the application using Docker

