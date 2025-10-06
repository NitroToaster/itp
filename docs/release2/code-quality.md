# Code Quality

This document defines our team’s principles for writing high-quality code.
Our focus areas are Readability, Performance, Maintainability, Reliability/Resiliency, and Security. All team members are responsible for upholding these standards across all code contributions.

## Readability

- Readable code is easy to understand, review, and modify
- It should clearly express intent and minimize confusion

### Guidelines

- Use clear, descriptive names for variables, methods, and classes
- Keep methods and classes short and focused on one responsibility
- Use comments to explain why, not what, and avoid restating obvious code
- Structure code with logical grouping and consistent indentation
- 

## Performance

Performance ensures our application runs efficiently without wasting resources. Code should be optimized where it matters, without sacrificing readability.

### Guidelines

- Reuse objects or results where practical
- Use appropriate data structures and algorithms for the task

## Maintainability

Maintainable code can be easily extended, refactored, or fixed without breaking existing functionality. It encourages long-term project health and team scalability.

### Guidelines 

- Write modular, loosely coupled code — each component should do one thing well
- Refactor regularly to reduce duplication or complexity
- Update documentation and tests alongside code changes; all new functionality must be followed by adequate testing in the same PR
- Ensure code reviews focus on structure and clarity
<<<<<<< HEAD
- The team uses Maven extensions JaCoCo, Spotbugs and Checkstyle to ensure test coverage, which is essential for code maintainability
=======
>>>>>>> origin/main

## Reliability / Resiliency

Reliable code behaves predictably under normal and exceptional conditions. Resilient systems recover gracefully from errors and unexpected inputs.

### Guidelines

- Handle exceptions properly — avoid silent failures or generic catches
- Test edge cases, error scenarios, and boundary conditions
- Keep error messages informative but user-safe, with no sensitive data

## Security

Security must be considered at every stage of development to protect data and prevent vulnerabilities. This will become more relevant when we use an API in the next milestone.

### Guidelines

- Use secure protocols and libraries (HTTPS, modern encryption, etc.)
- Avoid hardcoding secrets, credentials, or API keys
- Regularly review code and dependencies for security risks



## Continuous Improvement

- Treat code quality as a shared responsibility
- Discuss improvements in weekly meetings
- Refine this document as the project and team evolve