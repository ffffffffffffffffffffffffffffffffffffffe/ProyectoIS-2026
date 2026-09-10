# Plataforma de Prácticas Pedagógicas

Proyecto semestral de Ingeniería de Software 2026, desarrollado en la Universidad del Bío-Bío.

La plataforma permite centralizar portafolios y evidencias de prácticas pedagógicas, administrar usuarios y roles, y facilitar el seguimiento de estudiantes por responsables autorizados.

## Integrantes

- Damaris Bugueño Cornejo
- Daniel Jesús Canto Moreno
- Felipe Andrés Escobar Stuardo

## Tecnologías

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL
- Gradle con Groovy DSL

El equipo utiliza Scrum y Trello para organizar el Product Backlog, el Sprint Backlog y las tareas.

## Funcionalidades del Sprint 1

- HU-01: crear y consultar portafolios del estudiante.
- HU-02: subir evidencias de práctica en formato PDF.
- HU-03: consultar y descargar evidencias como responsable autorizado.
- HU-04: gestionar roles y permisos de usuario.
- HU-05: asociar responsables a estudiantes por asignatura y período.

La plataforma también cuenta con inicio y cierre de sesión y un listado de miembros exclusivo para el administrador.

## Requisitos

Antes de iniciar, debes tener:

- JDK 25.
- Git.
- MySQL Server instalado y en ejecución.
- IntelliJ IDEA u otro entorno compatible con Java y Gradle.
- MySQL Workbench, si deseas administrar la base de datos mediante una interfaz gráfica.

MySQL Workbench es una herramienta de administración: también necesitas el servidor MySQL.

No es necesario instalar Gradle por separado; el proyecto incluye Gradle Wrapper.

## 1. Obtener el proyecto

Clona el repositorio desde IntelliJ IDEA mediante la opción de obtener un proyecto desde control de versiones.

También puedes utilizar una terminal, reemplazando la URL por la de este repositorio:

```powershell
git clone URL_DEL_REPOSITORIO
cd ProyectoIS-2026
```

Abre la carpeta que contiene `build.gradle` y espera a que finalice la sincronización de Gradle.

Configura el JDK del proyecto y la JVM de Gradle con Java 25.

Si ya tienes el proyecto, revisa tus cambios locales antes de actualizarlo:

```powershell
git status
git pull --ff-only
```

## 2. Preparar MySQL

Conéctate a tu servidor desde MySQL Workbench y ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS proyecto_is_2026
    CHARACTER SET utf8mb4;
```

Si tienes un respaldo del proyecto, impórtalo desde:

**Server → Data Import → Import from Self-Contained File**

Selecciona el archivo `.sql`, el esquema de destino `proyecto_is_2026` y ejecuta la importación.

Comprueba las tablas:

```sql
USE proyecto_is_2026;
SHOW TABLES;
```

Importar el código desde GitHub no importa automáticamente la base de datos.

## 3. Configurar la conexión

El archivo `src/main/resources/application.properties` debe contener la configuración de conexión:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/proyecto_is_2026
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update

server.port=8081
```

Si tu servidor MySQL utiliza otro host o puerto, ajusta la URL.

La opción `ddl-auto=update` permite crear o actualizar las tablas a partir de las entidades durante el desarrollo. No recupera los datos de otro computador.

### Variables de entorno en IntelliJ IDEA

1. Abre **Run → Edit Configurations**.
2. Selecciona la configuración de `DemoApplication`.
3. Busca **Environment variables**. Si no aparece, habilítala desde **Modify options**.
4. Agrega:

| Variable | Valor |
|---|---|
| `DB_USER` | Tu usuario de MySQL |
| `DB_PASSWORD` | La contraseña de ese usuario |

Estas credenciales corresponden a MySQL, no a la cuenta con la que iniciarás sesión en la página.

No guardes contraseñas ni tokens en archivos que se suban a GitHub.

## 4. Preparar la cuenta de administrador

Si importaste un respaldo que ya contiene una cuenta de administrador, utiliza esa cuenta.

Si estás comenzando con una base de datos vacía, el proyecto incluye una inicialización mediante el perfil `bootstrap`.

Agrega temporalmente estas variables a la configuración de `DemoApplication`, además de las variables de MySQL:

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `bootstrap` |
| `ADMIN_EMAIL` | Correo de la nueva cuenta administradora |
| `ADMIN_PASSWORD` | Contraseña inicial de al menos 8 caracteres |

Ejecuta la aplicación una vez. Después, detén la aplicación y retira las tres variables de inicialización.

Conserva `DB_USER` y `DB_PASSWORD` para las siguientes ejecuciones.

La inicialización no cambia la contraseña ni concede permisos adicionales si ya existe una cuenta con ese correo.

## 5. Ejecutar la aplicación

Desde IntelliJ IDEA, abre:

```text
src/main/java/com/example/demo/DemoApplication.java
```

Ejecuta su método `main`.

También puedes iniciar desde PowerShell, siempre que las variables de entorno estén configuradas en esa terminal:

```powershell
.\gradlew.bat bootRun
```

Las variables configuradas en una ejecución de IntelliJ no se transfieren automáticamente a su terminal.

Cuando el inicio termine correctamente, abre:

**http://localhost:8081**

La página de inicio de sesión está disponible en:

**http://localhost:8081/login**

La aplicación utiliza el puerto **8081**. MySQL utiliza el puerto configurado en la URL de conexión, normalmente **3306**.

## 6. Utilizar la plataforma

### Administrador

- Crear usuarios.
- Consultar los miembros del sistema.
- Asignar y retirar roles.
- Crear, desactivar y reactivar asignaciones de seguimiento.

### Estudiante

- Crear y consultar sus portafolios.
- Subir evidencias PDF.
- Consultar y descargar sus evidencias.

### Responsables de seguimiento

Los roles disponibles son:

- `PROFESOR`
- `COLABORADOR_EVALUADOR`
- `TUTOR`

Para consultar un portafolio, el responsable debe tener una asignación activa al estudiante que coincida con la asignatura y el período del portafolio.

El rol del responsable también debe corresponder al tipo de relación de la asignación.

El menú superior muestra el nombre de la persona que inició sesión. Desde ese menú puede cerrar la sesión.

## 7. Archivos de evidencias

La configuración de almacenamiento es:

```properties
app.evidencias.directorio=${EVIDENCIAS_DIR:./datos/evidencias}
```

De forma predeterminada, los archivos se guardan en `datos/evidencias`, relativa al directorio desde el que se ejecuta la aplicación.

Puedes definir la variable `EVIDENCIAS_DIR` para utilizar otra ubicación.

La carpeta se crea cuando se guarda una evidencia correctamente.

La carga admite archivos PDF de hasta 10 MB. La comprobación de cabecera del archivo no equivale a un análisis antivirus ni a una validación completa del documento.

### Cambiar de computador

Para conservar todo el contenido necesitas:

1. El código actualizado desde GitHub.
2. Un respaldo de MySQL.
3. Una copia de los archivos almacenados en `datos/evidencias`, o en la ubicación configurada.
4. Configurar las variables de entorno en el nuevo equipo.

El respaldo SQL contiene los registros de las evidencias, pero no los PDF almacenados en el disco.

## 8. Ejecutar las pruebas

Desde IntelliJ IDEA, ejecuta las clases ubicadas en:

```text
src/test/java
```

Las pruebas que cargan el contexto de Spring y utilizan MySQL necesitan las variables `DB_USER` y `DB_PASSWORD` en su configuración de ejecución.

Desde PowerShell, con esas variables disponibles:

```powershell
.\gradlew.bat test
```

Para ejecutar únicamente las pruebas de seguimiento:

```powershell
.\gradlew.bat test --tests "com.example.demo.asignacion.SeguimientoServiceTest"
```

El informe de Gradle queda en:

```text
build/reports/tests/test/index.html
```

Se recomienda utilizar una base de datos de pruebas separada. Puede seleccionarse mediante la variable `SPRING_DATASOURCE_URL` en la configuración de ejecución de los tests.

## Problemas frecuentes

### `Unknown database 'proyecto_is_2026'`

La base de datos no existe en el servidor al que intenta conectarse la aplicación.

Crea o importa el esquema y revisa el nombre configurado en la URL.

### `Access denied for user`

Revisa el usuario y la contraseña de MySQL y comprueba que puede conectarse al mismo servidor desde Workbench.

### El error muestra `${DB_USER}` como usuario

La variable de entorno no está disponible en esa ejecución.

Configúrala en la aplicación, prueba o terminal desde la que estés ejecutando.

### `Unable to determine Dialect without JDBC metadata`

Revisa las líneas anteriores del registro. En los problemas de conexión observados, este mensaje aparece como consecuencia de no poder acceder a MySQL.

### El puerto 8081 está ocupado

Detén la ejecución anterior de la aplicación o el proceso que esté utilizando ese puerto.

### Una evidencia aparece, pero no se puede descargar

Comprueba que el archivo PDF también exista en el directorio de almacenamiento del computador actual.

### No aparecen portafolios para un responsable

Comprueba:

- Que exista el portafolio.
- Que la asignación esté activa.
- Que corresponda al estudiante correcto.
- Que coincidan asignatura y período.
- Que el usuario tenga el rol correspondiente a la relación asignada.
