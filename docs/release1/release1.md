# FridgeApp – Project Documentation

## Project Overview

FridgeApp is a Java-based application designed to manage the contents of a refrigerator. The application allows users to keep track of different items, organized by type, quantity, unit of measurement, and expiration date. The main purpose of the project is to provide a simple and efficient way for users to monitor food inventory. 

## Goals

Enable users to register and view items in the fridge.

Sort and filter items by category and expiration date.

Provide a graphical user interface (GUI) for easy interaction.

Ensure the core logic is separated from the user interface for modularity and maintainability.

## System Architecture

The project follows a modular architecture with a separation between the core logic and the FXUI (JavaFX User Interface):

### Core module:
Contains the main business logic of the fridge, including handling items, their attributes, and operations such as sorting and expiration checks.

### Fxui module:
Provides the graphical user interface built with JavaFX, which communicates with the core module to display and manage data.

This separation ensures that the application logic can be tested independently of the user interface.

## Technologies used

Programming Language: Java

Build Tool: Maven

GUI Framework: JavaFX

Testing: JUnit

Version Control: Git (GitHub)

Documentation & Reporting: Maven Site, JaCoCo for code coverage

### AI Tools Used (see docs/ai-tools.md)

GitHub Copilot (VS Code): code suggestions and boilerplate generation

ChatGPT: explanations, documentation, and user stories

Claude.ai: refining code and reviewing written content

## Project structure

.github/                 → Issue and pull request templates  
docs/                    → Documentation and release notes  
fridgeapp/               → Main project folder  
 ├── core/               → Core logic module  
 │   ├── src/main/java   → Core functionality classes  
 │   ├── src/test/java   → Unit tests for core  
 │   └── target/         → Build outputs and reports  
 ├── fxui/               → JavaFX user interface module  
 │   ├── src/main/java   → UI classes  
 │   ├── src/main/resources → UI resources   
 │   ├── src/test/java   → Unit tests for UI  
 │   └── target/         → Build outputs and reports  
 └── target/             → Aggregated reports   
