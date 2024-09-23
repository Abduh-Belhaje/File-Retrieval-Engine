# File Retrieval Engine

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Usage](#usage)

## Introduction
The **File Retrieval Engine** is a Java-based application designed to efficiently index and retrieve files based on user queries. Leveraging multithreading for accelerated indexing and search capabilities, it returns the top 10 most relevant files that match a given query. The engine is built following Application Layering architecture principles, ensuring modularity and maintainability.

## Features
- Multithreaded indexing for faster file processing
- Efficient search algorithm to retrieve the top 10 relevant files
- Modular architecture for easy maintenance and scalability
- Customizable indexing and searching strategies
- **Support for both app interface and REST API** with a shared index for flexible integration and usage

## Architecture
The File Retrieval Engine follows an Application Layering architecture, which includes the following layers:
1. **Presentation Layer**: Manages user interactions through both an app interface and a REST API.
2. **Application Layer**: Contains business logic and coordinates data flow.
3. **Data Layer**: Handles file storage, indexing, and retrieval mechanisms.

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven (for dependency management and building the project)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Abduh-Belhaje/File-Retrieval-Engine.git

2. Navigate to the project directory:
   ```bash
   cd File-Retrieval-Engine

3. Build the project using Maven:
   ```bash
   mvn clean install
   
4. Run the application:
   ```bash
   java -jar target/File-Retrieval-Engine-1.0-SNAPSHOT.jar <nbOfThreads> src/main/java/resources/config.json
