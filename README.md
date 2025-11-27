# 🧩 Microservicios con Spring Boot y Apache Kafka
Este proyecto implementa una arquitectura basada en microservicios utilizando **Spring Boot**, **Apache Kafka** y bases de datos independientes para cada servicio.  
Los tres microservicios principales son:

- **product_service** → Base de datos: `ecommerce`
- **order_service** → Base de datos: `ecommerce_orders`
- **inventory_service** → Base de datos: `ecommerce_inventory`

La comunicación entre microservicios se realiza de forma asíncrona mediante **Kafka**, asegurando un flujo confiable.

---

## 🚀 Arquitectura General del Proyecto


---

## 📦 Microservicios

### 1️⃣ **product_service**
Responsable de gestionar la información de productos:

- Crear productos
- Listar productos
- Consultar productos por ID
- Actualizar y eliminar productos

Base de datos asociada: **ecommerce**

---

### 2️⃣ **order_service**
Administra el proceso de creación de órdenes.  
Cada orden generada se publica en un topic Kafka para ser procesada por el servicio de inventario.

Flujo interno:

1. Cliente envía una orden
2. order_service valida y guarda la orden
3. Publica evento Kafka → `orders-topic`
4. Retorna respuesta al cliente

Base de datos: **ecommerce_orders**

---

### 3️⃣ **inventory_service**
Escucha eventos publicados en Kafka sobre nuevas órdenes.  
Por cada mensaje:

1. Determina los productos asociados
2. Verifica stock
3. Actualiza inventario
4. Registra movimientos

Base de datos: **ecommerce_inventory**

---

## 🔄 Flujo de Comunicación (Kafka)


### Topic principal:
- **orders-topic** → Contiene los datos de las órdenes generadas

### order_service actúa como:
- **Producer** (publica mensajes JSON en Kafka)

### inventory_service actúa como:
- **Consumer** (procesa los mensajes y descuenta stock)

---

## 📂 Estructura General del Proyecto


Cada microservicio mantiene su propia estructura con:

- Controllers
- Services
- Repositories
- DTOs
- Configuración de Kafka
- Excepciones personalizadas
- Entidades y mapeos JPA

---

## 🛠 Tecnologías Utilizadas

- **Java 17+**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Apache Kafka**
- **PostgreSQL**
- **Docker**
- **Kafka**

---

## 🐳 Kafka con Docker Compose

Este proyecto incluye únicamente Kafka levantado en Docker, dado que las bases de datos se gestionan por separado.

Ejemplo de uso:

```bash
docker compose up -d
docker compose up -d
mvn spring-boot:run
```
> **Nota:** Como se trabajó en en IntelliJ IDEA también se puede correr por la interfaz gráfica que se observa en las capturas dentro de recursos.

# Microservicios de E-commerce

Este proyecto implementa microservicios de e-commerce con arquitectura en capas, gestión de excepciones, relaciones de entidades y mensajería event-driven usando Apache Kafka en modo KRaft. La documentación detalla los pasos seguidos para su construcción, así como conceptos clave.

---

## Paso 1: Microservicio product_service

El microservicio sigue la **arquitectura en capas**, separando responsabilidades:


# Sección 00 · Generar el proyecto product_service

## 1. Objetivo

Crear el esqueleto del microservicio `product_service` con Spring Boot 3, listo para ejecutar.

---

## 2. Comandos a ejecutar

```bash
# 1. Preparar carpeta de trabajo
mkdir -p ~/workspace && cd ~/workspace

# 2. Generar el proyecto con Spring Initializr (alternativa CLI)
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d groupId=dev.ychuquimia \
  -d artifactId=product_service \
  -d name=product_service \
  -d packageName=dev.ychuquimia.product_service \
  -d javaVersion=17 \
  -d dependencies=web \
  -o product-service.zip

# 3. Descomprimir y entrar al directorio
unzip -q product-service.zip
cd product-service

# 4. Ejecutar la aplicación para validar la generación
mvn spring-boot:run
```
> **Nota:** También se puede dirigir al sitio https://start.spring.io/ Generar, descomprimir y abrir en Intellij IDEA o en un editor de su preferencia.

## 4. Explicación detallada

1. **Generación**  
   Spring Initializr construye un proyecto Maven con `pom.xml`, estructura `src/` y la clase `product_serviceApplication` anotada con `@SpringBootApplication`.
2. **Exploración**  
   Abre el proyecto en IntelliJ IDEA. Verifica la estructura `src/main/java` y `src/main/resources` junto con el archivo `application.properties`.
3. **Ejecución**  
   Al ejecutar `mvn spring-boot:run`, Maven descarga dependencias la primera vez y arranca la aplicación mostrando el banner de Spring Boot.
4. **Validación rápida**  
   Visita `http://localhost:8080`. Un mensaje 404 indica que el servidor está arriba (no hay endpoints todavía, pero la aplicación responde).

---

# Sección 02 · PostgreSQL con Docker Compose

## 1. Objetivo

Levantar PostgreSQL 15 mediante Docker Compose, validar la conexión y dejar preparado el entorno de base de datos.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en la raíz del proyecto (ajusta la ruta si tu carpeta es distinta)
cd ~/workspace/product-service

# 2. Crear el archivo docker-compose.yml
cat <<'EOF' > docker-compose.yml
services:
  postgres:
    image: postgres:15-alpine
    container_name: product-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: ecommerce
      POSTGRES_USER: ecommerce_user
      POSTGRES_PASSWORD: ecommerce_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
EOF

# 3. Levantar el contenedor
docker compose up -d

# 4. Verificar estado
docker compose ps

# 5. Inspeccionar logs iniciales (opcional)
docker compose logs -f postgres
```

---

## 3. Desglose del comando

| Comando | Descripción |
|---------|-------------|
| `docker-compose.yml` | Define el servicio `postgres` usando la imagen oficial 15-alpine con credenciales y volumen persistente. |
| `docker compose up -d` | Descarga la imagen (si es necesario) y arranca el contenedor en segundo plano. |
| `docker compose ps` | Muestra el estado de los servicios definidos en el archivo Compose. |
| `docker compose logs -f postgres` | Sigue los logs en tiempo real para confirmar que PostgreSQL quedó listo para aceptar conexiones. |

---
# Sección 03 · Entidad y repositorio con Spring Data JPA

## 1. Objetivo

Configurar Spring Data JPA, mapear la entidad `Product` y crear el repositorio que permitirá persistir datos en PostgreSQL.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Abrir pom.xml con tu editor y agregar Spring Data JPA + PostgreSQL
code pom.xml  # Usa tu editor preferido (nano, vim, IntelliJ, etc.)

# 3. Cambiar a application.yml si aún usas application.properties
rm -f src/main/resources/application.properties
cat <<'EOF' > src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: ecommerce_user
    password: ecommerce_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
EOF

# 4. Crear la entidad Product
mkdir -p src/main/java/dev/ychuquimia/product_service/model


# 5. Crear el repositorio
mkdir -p src/main/java/dev/ychuquimia/product_service/repository

# 6. Verificar el arranque y la creación de la tabla
mvn spring-boot:run
```

---

## 3. Desglose del comando

| Paso | Descripción |
|------|-------------|
| Dependencias agregadas | `spring-boot-starter-data-jpa` provee JPA/Hibernate y `postgresql` actúa como driver JDBC. |
| `application.yml` | Configura la conexión a PostgreSQL, habilita `ddl-auto=update` para generar tablas y muestra SQL en consola. |
| `Product` | Entidad con campos básicos, auditoría de creación/actualización y restricciones (`nullable=false`, longitudes, precisión). |
| `ProductRepository` | Extiende `JpaRepository` para obtener métodos CRUD y agrega consulta derivada `findByNameContainingIgnoreCase`. |
| `mvn spring-boot:run` | Conecta a PostgreSQL, crea la tabla `products` y muestra los SQL de Hibernate. |

---

# Sección 04 · CRUD con Spring Data JPA

## 1. Objetivo

Conectar el controlador REST directamente con `ProductRepository` para lograr un CRUD funcional sobre la entidad `Product` persistida en PostgreSQL.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Abrir el controlador existente
code src/main/java/dev/ychuquimia/product_service/controller/ProductController.java
```

---

## 3. Desglose del comando

| Componente | Descripción |
|------------|-------------|
| `cd ~/workspace/product-service` | Navega al directorio del proyecto |
| `code src/main/.../ProductController.java` | Abre el controlador en el editor |
| `@RestController` | Marca la clase como controlador REST |
| `@RequestMapping("/api/products")` | Prefijo de ruta para todos los endpoints |
| `ProductRepository repository` | Inyección de dependencia del repositorio |
| `repository.findAll()` | Método de Spring Data JPA para obtener todos los registros |
| `repository.findById(id)` | Busca un registro por ID, retorna `Optional<Product>` |
| `repository.save(entity)` | Guarda o actualiza una entidad |
| `repository.deleteById(id)` | Elimina un registro por ID |
| `ResponseStatusException` | Lanza excepciones HTTP con códigos de estado |

---

# Sección 05 · Arquitectura en capas

## 1. Objetivo

Reorganizar el `product_service` para separar responsabilidades entre controller, service, repository y DTOs. Eliminaremos lógica de negocio del controlador y centralizaremos las operaciones CRUD en `product_service` usando un mapper manual.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Crear paquetes si no existen
dirs=("src/main/java/dev/ychuquimia/product_service/dto" \
      "src/main/java/dev/ychuquimia/product_service/mapper" \
      "src/main/java/dev/ychuquimia/product_service/exception")
for dir in "${dirs[@]}"; do mkdir -p "$dir"; done

# 3. Abrir los archivos indicados en tu IDE para actualizarlos

# 4. Mapper manual

**ProductMapper.java** (ubicar en `dev.ychuquimia.product_service.mapper`):

# 5. Service refactorizado

**product_service.java** (ubicar en `dev.ychuquimia.product_service.service`):

# 6. Excepción reutilizable

**ResourceNotFoundException.java** (ubicar en `dev.ychuquimia.product_service.exception`):

```

---
## 4. Explicación detallada

1. **Controller sin lógica de negocio**: recibe la petición, delega todo al service y retorna `ProductResponse`.
2. **Service como orquestador**: centraliza validaciones, búsquedas y conversiones a DTO.
3. **Mapper estático**: evita repetición y facilita futuros cambios (por ejemplo, añadir campos).
4. **Excepción compartida**: define un mensaje claro y permite un manejo uniforme en el handler global.

---

# Sección 06 · Validaciones con Bean Validation

## 1. Objetivo

Agregar validaciones a los DTOs de `product_service` usando Bean Validation, asegurando que las peticiones inválidas respondan con código `400 Bad Request`.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Editar pom.xml
code pom.xml

# 3. Crear archivo de mensajes de validación
code src/main/resources/ValidationMessages.properties

# 4. Actualizar application.yml
code src/main/resources/application.yml

# 5. Editar DTO ProductRequest
code src/main/java/dev/ychuquimia/product_service/dto/ProductRequest.java

# 6. Actualizar ProductController
code src/main/java/dev/ychuquimia/product_service/controller/ProductController.java

# 7. Recompilar y ejecutar
mvn clean spring-boot:run
```

---

## 3. Desglose del comando

| Componente | Descripción |
|------------|-------------|
| `spring-boot-starter-validation` | Dependencia que incluye Hibernate Validator y Jakarta Bean Validation API |
| `@Valid` | Activa la validación automática en parámetros de controller |
| `@NotBlank` | Valida que el string no sea null, vacío ni solo espacios |
| `@Size(max = 120)` | Limita la longitud máxima del string |
| `@NotNull` | Valida que el campo no sea null |
| `@DecimalMin` | Valida que el decimal sea mayor o igual al valor especificado |
| `@PositiveOrZero` | Valida que el número sea positivo o cero |
| `message = "{product.name.notblank}"` | Referencia a mensaje personalizado en ValidationMessages.properties |
| `spring.messages.basename` | Configura el archivo de mensajes personalizados |

---

## 4. Explicación detallada

### Paso 1: Agregar dependencia (si no se añadió antes)

Abre `pom.xml` en tu IDE y dentro del bloque `<dependencies>` añade:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
   </dependency>
   ```

### Paso 2: Crear ValidationMessages.properties

Ubicado en `src/main/resources/`:
```properties
product.name.notblank=El nombre es obligatorio
product.price.min=El precio debe ser mayor que cero
product.stock.min=El stock no puede ser negativo
```

### Paso 3: Configurar application.yml

```yaml
spring:
  messages:
    basename: ValidationMessages
```

### Paso 4: Actualizar ProductRequest con validaciones

**ProductRequest.java** (editar archivo en `dev.ychuquimia.product_service.dto`):

# Sección 07 · Manejo global de excepciones

## 1. Objetivo

Crear un manejador global de excepciones (`@RestControllerAdvice`) que capture errores del sistema y devuelva respuestas JSON consistentes con códigos HTTP apropiados.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Crear paquete exception
mkdir -p src/main/java/dev/ychuquimia/product_service/exception

# 3. Crear ErrorResponse
code src/main/java/dev/ychuquimia/product_service/exception/ErrorResponse.java

# 4. Crear ResourceNotFoundException
code src/main/java/dev/ychuquimia/product_service/exception/ResourceNotFoundException.java

# 5. Crear GlobalExceptionHandler
code src/main/java/dev/ychuquimia/product_service/exception/GlobalExceptionHandler.java

# 6. Actualizar product_service para lanzar excepciones
code src/main/java/dev/ychuquimia/product_service/service/product_service.java

# 7. Recompilar y ejecutar
mvn clean spring-boot:run
```

---

## 3. Desglose del comando

| Componente | Descripción |
|------------|-------------|
| `@RestControllerAdvice` | Anotación que marca la clase como manejador global de excepciones para controladores REST |
| `@ExceptionHandler` | Define qué excepción captura cada método del handler |
| `ResourceNotFoundException` | Excepción personalizada para recursos no encontrados (404) |
| `MethodArgumentNotValidException` | Excepción de Spring cuando falla Bean Validation |
| `ErrorResponse` | DTO que define el formato uniforme de errores |
| `HttpServletRequest` | Permite acceder a información de la petición (URI, headers) |
| `ResponseEntity.status()` | Construye respuesta HTTP con código de estado específico |
| `getBindingResult()` | Obtiene los errores de validación de campos |

---

---

## 4. Explicación detallada

```bash
### Paso 1: Crear GlobalExceptionHandler
**GlobalExceptionHandler.java** (crear en `dev.ychuquimia.product_service.exception.GlobalExceptionHandler`):

### Paso 2: Crear ResourceNotFoundException
**ResourceNotFoundException.java** (crear en `dev.ychuquimia.product_service.exception.ResourceNotFoundException`):

### Paso 3: Crear GlobalExceptionHandler
**GlobalExceptionHandler.java** (crear en `dev.ychuquimia.product_service.exception.GlobalExceptionHandler`):

```

```bash

**Resultado**: Todas las excepciones retornan JSON consistente con timestamp, status, código, mensaje y path.
```
---
# Sección 08 · Relación Product–Category (1:N)

## 1. Objetivo

Introducir la entidad `Category`, establecer una relación `@ManyToOne` desde `Product` y exponer endpoints REST para gestionar categorías y consultar productos por categoría.

---

## 2. Comandos a ejecutar

```bash
# 1. Ubicarse en el proyecto
cd ~/workspace/product-service

# 2. Crear paquete exception si no existe
mkdir -p src/main/java/dev/ychuquimia/product_service/exception

# 3. Actualizar DTOs, mapper y servicios en tu IDE

# 4. Actualizar DTOs, mapper y servicios en tu IDE
**Category.java** (crear en `dev.ychuquimia.product_service.model`):

# 5. Ajustes en Product
**Product.java** - Agregar imports y campo `category` en la entidad existente:

# 6. Repositorios
**CategoryRepository.java** (crear en `dev.ychuquimia.product_service.repository`):

# 7. Actualizar
**ProductRepository.java** (actualizar en `dev.ychuquimia.product_service.repository`):

# 8. Crear DTO
**CategoryRequest.java** (crear en `dev.ychuquimia.product_service.dto`):

# 9. Crear
**CategoryResponse.java** (crear en `dev.ychuquimia.product_service.dto`):

# 10. Actualizar DTO
**ProductRequest.java** (actualizar en `dev.ychuquimia.product_service.dto`):

# 11. Actualizar DTO
**ProductResponse.java** (actualizar en `dev.ychuquimia.product_service.dto`):

# 11. Actualizar mapper
**ProductMapper.java** (actualizar en `dev.ychuquimia.product_service.mapper`):

# 12. Actualización de product_service
**product_service.java** (actualizar en `dev.ychuquimia.product_service.service`):

# 13. CategoryController
**CategoryController.java** (crear en `dev.ychuquimia.product_service.controller`):

# 14. Crear CategoryService
**CategoryService.java** (crear en `dev.ychuquimia.product_service.service`):

# 15. Crear CategoryAlreadyExistsException 
**CategoryAlreadyExistsException.java** (crear en `dev.ychuquimia.product_service.exception`):

# 16. Actualización del handler global
**GlobalExceptionHandler.java** - Agregar este método a la clase existente en `dev.ychuquimia.product_service.exception`:
```

---

## 4. Explicación detallada

1. **Entidad Category**: recibe anotaciones estándar y mantiene la colección de productos (solo para navegación interna).
2. **Relación en Product**: la columna `category_id` es obligatoria; se maneja con fetch LAZY para evitar cargas innecesarias.
3. **Endpoints nuevos**: exponen la creación de categorías y la consulta de productos por categoría.
4. **Validaciones extra**: previenen duplicados (`existsByNameIgnoreCase`) y responden con error usando el handler global (añade un handler para `CategoryAlreadyExistsException`).

---

# Sección 09 · Docker Compose para Kafka

## Objetivo

Desplegar Apache Kafka en modo KRaft usando Docker Compose, creando una infraestructura local de mensajería moderna y simplificada para desarrollo y testing de microservicios event-driven.

---

## Comandos a ejecutar

```bash
# 1. Navegar al directorio de trabajo y crear directorio para Kafka
cd ~/workspace
mkdir -p kafka-infrastructure
cd kafka-infrastructure

# 2. Crear archivo docker-compose.yml con toda la configuración
cat > docker-compose.yml << 'EOF'
services:
  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
    volumes:
      - kafka-data:/var/lib/kafka/data

volumes:
  kafka-data:
EOF

# 3. Descargar imagen de Docker (primera vez, puede tardar)
docker compose pull

# 4. Iniciar servicio en background
docker compose up -d

# 5. Verificar que Kafka está corriendo
docker compose ps

# 6. Ver logs de Kafka
docker compose logs kafka

# 7. Seguir logs en tiempo real (Ctrl+C para salir)
docker compose logs -f kafka

# 8. Verificar que Kafka está listo
docker exec -it kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# 9. Entrar al contenedor de Kafka (para ejecutar comandos CLI)
docker exec -it kafka bash

# 10. Dentro del contenedor, listar topics (vacío por ahora)
kafka-topics --bootstrap-server localhost:9092 --list

# 11. Salir del contenedor
exit

# 12. Detener servicio (mantiene datos)
docker compose stop

# 13. Iniciar servicio nuevamente
docker compose start

# 14. Detener y eliminar servicio (mantiene volumen)
docker compose down

# 15. Detener, eliminar servicio y volumen (limpieza completa)
docker compose down -v
```

# Sección 10 · Kafka CLI - Crear Topics

## Objetivo

Utilizar las herramientas de línea de comandos de Kafka (Kafka CLI) para crear, listar, describir y eliminar topics, preparando la infraestructura de mensajería para el dominio e-commerce.

---

## Comandos a ejecutar

```bash
# 1. Verificar que Kafka está corriendo
docker compose ps

# 2. Entrar al contenedor de Kafka
docker exec -it kafka bash

# 3. Listar topics existentes (vacío inicialmente)
kafka-topics --bootstrap-server localhost:9092 --list

# 4. Crear topic para eventos de productos
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.products.created \
  --partitions 5 \
  --replication-factor 1

# 5. Crear topic para eventos de órdenes (placed)
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.orders.placed \
  --partitions 5 \
  --replication-factor 1

# 6. Crear topic para eventos de órdenes (confirmed)
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.orders.confirmed \
  --partitions 5 \
  --replication-factor 1

# 7. Crear topic para eventos de órdenes (cancelled)
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.orders.cancelled \
  --partitions 5 \
  --replication-factor 1

# 8. Crear topic para eventos de inventario
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.inventory.updated \
  --partitions 5 \
  --replication-factor 1

# 9. Listar todos los topics creados
kafka-topics --bootstrap-server localhost:9092 --list

# 10. Describir un topic específico
kafka-topics --bootstrap-server localhost:9092 \
  --describe \
  --topic ecommerce.orders.placed

# 11. Ver configuración de un topic
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name ecommerce.orders.placed \
  --describe

# 12. Modificar número de particiones de un topic
kafka-topics --bootstrap-server localhost:9092 \
  --alter \
  --topic ecommerce.products.created \
  --partitions 5

# 13. Eliminar un topic (CUIDADO: operación destructiva)
kafka-topics --bootstrap-server localhost:9092 \
  --delete \
  --topic ecommerce.products.created

# 14. Salir del contenedor
exit
```

**Salida esperada de `--list`**:

```
ecommerce.inventory.updated
ecommerce.orders.cancelled
ecommerce.orders.confirmed
ecommerce.orders.placed
ecommerce.products.created
```

---

# Sección 11 · Configurar spring-kafka en product-service

## Objetivo

Integrar la dependencia spring-kafka en el proyecto product-service existente, configurar el productor de Kafka mediante application.yml y verificar que KafkaTemplate se puede inyectar correctamente como bean de Spring.

---

## Comandos a ejecutar

```bash
# 0. PREREQUISITO: Verificar que Kafka y PostgreSQL están corriendo
docker compose ps
# Debería mostrar kafka y postgres "Up"
# Si no están corriendo, navegar a ~/workspace/kafka-infrastructure y ejecutar:
# docker compose up -d

# 1. Verificar que la base de datos 'ecommerce' existe (creada en Clase 2)
docker exec -it postgres psql -U postgres -c "\l" | grep ecommerce

# Si no existe, crearla:
# docker exec -it postgres psql -U postgres -c "CREATE DATABASE ecommerce;"

# 2. Navegar al directorio del proyecto product-service
# (Asume que product-service está en el directorio de trabajo de las clases 2-3)
cd ~/workspace/product-service

# 3. Editar pom.xml para agregar dependencia spring-kafka
# (Ver sección "Desglose del comando" para el contenido exacto)

# 4. Recargar dependencias Maven
mvn clean install

# 5. Editar src/main/resources/application.yml
# (Ver sección "Desglose del comando" para configuración)

# 6. Ejecutar la aplicación para verificar que inicia sin errores
mvn spring-boot:run

# 7. Verificar logs de inicio (buscar "KafkaProducer")
# Deberías ver: "Started KafkaProducerFactory"
```

---

## Desglose del comando

### 1. Dependencia Maven (pom.xml)

Agregar dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

Spring Boot ya define la versión compatible de spring-kafka en su BOM (Bill of Materials), garantizando compatibilidad entre todas las dependencias de Spring.

### 2. Configuración del Producer (application.yml)

Agregar al final del archivo:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.type.mapping: productCreatedEvent:dev.ychuquimia.product_service.kafka.event.ProductCreatedEvent
```

**JsonSerializer vs StringSerializer**:

- `JsonSerializer`: Spring convierte automáticamente objetos Java a JSON
- `StringSerializer`: Requeriría serializar manualmente con ObjectMapper

### 3. Verificación de KafkaTemplate

Spring Boot autoconfigura `KafkaTemplate` como bean cuando detecta:

1. Dependencia spring-kafka en classpath
2. Configuración `spring.kafka.bootstrap-servers` en application.yml

No se requiere clase `@Configuration` adicional.

---

# Sección 12 · Producer en product-service

## Objetivo

Implementar un productor de eventos Kafka en product-service que publique eventos ProductCreatedEvent al topic ecommerce.products.created cada vez que se crea un producto, integrando KafkaTemplate con la capa de servicio.

---

## Comandos a ejecutar

```bash
# 1. Verificar que Kafka está corriendo
docker compose ps

# 2. Crear el topic ecommerce.products.created
docker exec -it kafka bash
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.products.created \
  --partitions 5 \
  --replication-factor 1

# 3. Verificar topic creado
kafka-topics --bootstrap-server localhost:9092 --list

# 4. Salir del contenedor
exit

# 5. Navegar al proyecto product-service
cd ~/workspace/product-service

# 6. Crear estructura de paquetes
mkdir -p src/main/java/dev/ychuquimia/product_service/kafka/event
mkdir -p src/main/java/dev/ychuquimia/product_service/kafka/producer

# 7. Crear clases Java (ver sección "Desglose del comando")
# - ProductCreatedEvent.java
# - ProductEventProducer.java

# 8. Modificar product_service.java para publicar eventos

# 9. Ejecutar aplicación
mvn spring-boot:run

# 10. En otra terminal, consumir desde CLI para verificar
docker exec -it kafka bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic ecommerce.products.created \
  --from-beginning \
  --property print.key=true

# 11. Crear una categoría primero (si no existe)
Verificar en la seccion de Recursos

# 12. Crear un producto vía REST
Verificar en la seccion de Recursos

# 13. Crear ProductCreatedEvent
**ProductCreatedEvent.java** (crear en `dev.ychuquimia.product_service.kafka.event.ProductCreatedEvent`):

# 14. Crear ProductEventProducer
**ProductEventProducer.java** (crear en `dev.ychuquimia.product_service.kafka.producer.ProductEventProducer`):

# 15. Modificar product_service 
**product_service.java** (actualizar en `dev.ychuquimia.product_service.service`):

```

## Flujo Completo

```bash
1. Cliente → POST /api/products
2. ProductController recibe request
3. product_service.create():
   a. Valida categoría (BD)
   b. Crea producto (BD)
   c. Guarda en BD (commit transaction)
   d. Publica evento (Kafka) ← NUEVO
4. ProductController retorna response
5. Asíncronamente: Kafka confirma recepción
```

---

# Sección 13 · Crear order_service

## Objetivo

Crear un segundo microservicio (order_service) desde cero usando Spring Initializr, configurar base de datos PostgreSQL independiente, implementar entidad Order con CRUD básico y preparar la estructura para integrar Kafka.

---

## Comandos a ejecutar

```bash
# 1. Navegar al directorio de trabajo
cd ~/workspace

# 2. Generar proyecto con Spring Initializr (desde línea de comandos)
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,postgresql,validation \
  -d groupId=dev.ychuquimia \
  -d artifactId=order_service \
  -d name=order_service \
  -d description="Microservicio de gestión de órdenes para e-commerce" \
  -d packageName=dev.ychuquimia.order_service \
  -d javaVersion=17 \
  -d type=maven-project \
  -o order_service.zip

# 3. Descomprimir y entrar al directorio
unzip order_service.zip
cd order_service

# 4. Crear base de datos en PostgreSQL
docker exec -it postgres psql -U postgres -c "CREATE DATABASE ecommerce_orders;"

# 5. Verificar base de datos creada
docker exec -it postgres psql -U postgres -c "\l"

# 6. Configurar application.yml (ver sección "Desglose del comando")

# 7. Crear estructura de paquetes
mkdir -p src/main/java/dev/ychuquimia/order_service/model/entity
mkdir -p src/main/java/dev/ychuquimia/order_service/model/dto
mkdir -p src/main/java/dev/ychuquimia/order_service/repository
mkdir -p src/main/java/dev/ychuquimia/order_service/service
mkdir -p src/main/java/dev/ychuquimia/order_service/controller

# 8. Crear clases Java (ver sección "Desglose del comando")

# 9. Compilar y ejecutar en puerto 8081
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# 10. Verificar que funciona
curl http://localhost:8081/api/orders

# 11. Crear una orden de prueba
curl -X POST http://localhost:8081/api/orders \

Los archivos de postman se encuentran en la seccion de recursos
```
---

### 1. Spring Initializr - Generación del proyecto

```bash
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,postgresql,validation \
  -d groupId=dev.ychuquimia \
  -d artifactId=order_service \
  -d javaVersion=17
```

### 2. Crear base de datos independiente

```bash
docker exec -it postgres psql -U postgres -c "CREATE DATABASE ecommerce_orders;"
```

```
product-service → ecommerce (PostgreSQL)
order_service   → ecommerce_orders (PostgreSQL)
```

### 3. Configuración application.yml

Crear `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: order_service

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:ecommerce_orders}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: ${SERVER_PORT:8081}

logging:
  level:
    dev.ychuquimia.order_service: DEBUG
    org.springframework.web: DEBUG
```

### 4. Entidad Order

Crear `src/main/java/dev/ychuquimia/order_service/model/entity/Order.java`:

### 5. OrderStatus

Crear `src/main/java/dev/ychuquimia/order_service/model/entity/OrderStatus.java`:

```java
public enum OrderStatus {
    PENDING,      // Estado inicial
    CONFIRMED,    // Transición: inventory-service valida stock
    CANCELLED     // Transición: inventory-service rechaza (sin stock)
}
```

### 6. OrderRequest

Crear `src/main/java/dev/ychuquimia/order_service/model/dto/OrderRequest.java`:

### 7. OrderResponse

Crear `src/main/java/dev/ychuquimia/order_service/repository/dto/OrderResponse.java`:

### 8. OrderRepository

Crear `src/main/java/dev/ychuquimia/order_service/repository/OrderRepository.java`:

### 9. OrderResponse

Crear `src/main/java/dev/ychuquimia/order_service/service/order_service.java`:

### 10. controller

Crear `src/main/java/dev/ychuquimia/order_service/controller/OrderController.java`:

### Arquitectura en capas (igual que product-service)

```
Controller → Service → Repository → Database

OrderController
    ├─> order_service
         ├─> OrderRepository
              ├─> PostgreSQL (ecommerce_orders)
```
### Ejecutar en puerto diferente

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Verificar ambos servicios corriendo**:

```bash
curl http://localhost:8080/api/products  # product-service
curl http://localhost:8081/api/orders    # order_service
```

---

# Seección 14 Producer en order_service

## Objetivo

Integrar spring-kafka en order_service, crear el evento OrderPlacedEvent, implementar OrderEventProducer y publicar eventos al topic ecommerce.orders.placed cada vez que se crea una orden, completando así la implementación de productores en ambos microservicios.

---

## Comandos a ejecutar

```bash
# 1. Navegar al proyecto order_service
cd ~/workspace/order_service

# 2. Agregar dependencia spring-kafka a pom.xml
# (Ver sección "Desglose del comando")

# 3. Recargar dependencias
mvn clean install

# 4. Crear topic ecommerce.orders.placed
docker exec -it kafka bash
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic ecommerce.orders.placed \
  --partitions 5 \
  --replication-factor 1

# 5. Verificar topic creado
kafka-topics --bootstrap-server localhost:9092 --list

# 6. Salir del contenedor
exit

# 7. Configurar Kafka en application.yml
# (Ver sección "Desglose del comando")

# 8. Crear estructura de paquetes para Kafka
mkdir -p src/main/java/dev/ychuquimia/order_service/kafka/event
mkdir -p src/main/java/dev/ychuquimia/order_service/kafka/producer

# 9. Crear clases Java (ver sección "Desglose del comando")
# - OrderPlacedEvent.java
# - OrderEventProducer.java

# 10. Modificar order_service para publicar eventos

# 11. Ejecutar order_service en puerto 8081
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# 12. En otra terminal, consumir eventos desde CLI
docker exec -it kafka bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic ecommerce.orders.placed \
  --from-beginning \
  --property print.key=true

# 13. Crear una orden
curl -X POST http://localhost:8081/api/orders \

El archivo postman se encuentra en recursos

```

---

## Desglose del comando

### 1. Dependencia Maven (pom.xml)

Agregar dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Mismo proceso que en product-service** (Lab 00).

### 2. Configuración Kafka (application.yml)

Agregar al final de `src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.type.mapping: orderPlacedEvent:dev.ychuquimia.order_service.kafka.event.OrderPlacedEvent
```

### 3. Evento OrderPlacedEvent

Crear `src/main/java/dev/ychuquimia/order_service/kafka/event/OrderPlacedEvent.java`:

### 4. Producer OrderEventProducer

Crear `src/main/java/dev/ychuquimia/order_service/kafka/producer/OrderEventProducer.java`:

### 5. Integración en order_service

Modificar `src/main/java/dev/ychuquimia/order_service/service/order_service.java`:

---


```bash
# Crear producto
curl -X POST http://localhost:8080/api/products \

# Crear orden
curl -X POST http://localhost:8081/api/orders \

```

---

> Este README documenta todo el flujo de construcción y configuración del microservicio, sin incluir los bloques de código completos que ya se encuentran en los scripts y archivos del proyecto.

Por:
Yber Chuquimia Ticona - Curso Spring Boot & Kafka