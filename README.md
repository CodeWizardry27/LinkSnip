# LinkSnip

A URL shortener built with Spring Boot and vanilla frontend.

## Features

- Shorten long URLs using Base62 encoding
- Custom aliases (optional)
- Link expiration (configurable)
- Click tracking and analytics
- Copy to clipboard
- Delete links

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Data JPA
- **Database:** H2 (dev) / MySQL (prod)
- **Frontend:** HTML, CSS, JavaScript

## How to Run

### Prerequisites
- Java 17+
- Maven

### Steps

```bash
git clone https://github.com/CodeWizardry27/LinkSnip.git
cd LinkSnip
mvn spring-boot:run
```

Open `http://localhost:8080` in your browser.

### Using MySQL (optional)

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/linksnip
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/shorten` | Create short URL |
| GET | `/api/urls` | List all URLs |
| GET | `/api/urls/{code}/stats` | Get click stats |
| DELETE | `/api/urls/{code}` | Delete a URL |
| GET | `/s/{code}` | Redirect to original |

## Screenshots

![LinkSnip](screenshot.png)
