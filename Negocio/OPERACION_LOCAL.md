# Operacion local

## Requisitos y arranque

Se necesita Java 25 y PostgreSQL activo. La configuración local predeterminada usa `localhost:5433`, la base `Negocio`, el usuario `postgres` y la contraseña `123456`.

Desde PowerShell, dentro de la carpeta `Negocio`, inicia la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:8080`.

## Clientes y pedidos

El cliente crea una cuenta desde **Crear cuenta de cliente**. La contraseña debe tener al menos 8 caracteres. Después puede agregar productos al carrito, ver cantidades y total, y enviar el pedido. El servidor usa la identidad de la sesión; no se confía en un ID de cliente enviado por el navegador.

El administrador o vendedor ve los pedidos nuevos; la lista se actualiza automáticamente. Al aprobar un pedido, pasa a **En preparación**. Al marcarlo listo aparece en **Listos para entregar**; al cobrar y confirmar la entrega se guarda la venta con su forma de pago y el pedido pasa a **Entregado**. En ese último paso no se vuelve a descontar stock.

La pestaña **Ventas y caja** muestra las ventas de mostrador y los pedidos entregados, con totales del día y del mes, filtros por fecha/búsqueda y exportación CSV. Las ventas directas se registran desde el formulario de esa pestaña y descuentan el stock disponible.

## Acceso de administrador

La aplicación no cambia contraseñas de cuentas existentes. En la base actual, `admin / cambiar123` fue rechazado, así que usa las credenciales existentes o crea un administrador adicional con un nombre nuevo.

En PowerShell, define el nombre y la contraseña antes de arrancar:

```powershell
$env:NEGOCIO_ADMIN_USERNAME = "admin-nuevo"
$env:NEGOCIO_ADMIN_PASSWORD = "una-clave-segura-de-al-menos-8-caracteres"
.\mvnw.cmd spring-boot:run
```

El seeder crea la cuenta si ese nombre todavía no existe y no modifica cuentas existentes. Puedes cambiar también la conexión de base de datos con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.

Las contraseñas antiguas se migran a BCrypt cuando se inicia sesión correctamente. Los valores predeterminados son solo para desarrollo local; antes de publicar el sistema, configura credenciales propias y HTTPS (`SESSION_COOKIE_SECURE=true`).
