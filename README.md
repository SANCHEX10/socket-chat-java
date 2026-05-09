# Socket Chat Java 💬

Sistema de chat bidireccional usando sockets TCP en Java con interfaces gráfica y por línea de comandos.

## 🚀 Características

✅ Comunicación bidireccional en tiempo real  
✅ Soporte para múltiples clientes simultáneos  
✅ Interfaz gráfica (Swing) y CLI  
✅ Notificaciones de conexión/desconexión  
✅ Arquitectura Cliente-Servidor con TCP  
✅ Thread-safe  

## 📁 Estructura

```
socket-chat-java/
├── src/
│   ├── Server.java           # Servidor base
│   ├── Client.java           # Cliente base
│   ├── gui/
│   │   ├── ServerGUI.java    # Servidor con interfaz gráfica
│   │   └── ClientGUI.java    # Cliente con interfaz gráfica
│   └── cli/
│       ├── ServerCLI.java    # Servidor con CLI
│       └── ClientCLI.java    # Cliente con CLI
├── compile.sh
└── README.md
```

## 🛠️ Requisitos

- Java 8 o superior
- Compilador javac

## 📦 Compilación

```bash
# Opción 1: Ejecutar script
chmod +x compile.sh
./compile.sh

# Opción 2: Compilar manualmente
javac src/*.java src/cli/*.java src/gui/*.java
```

## 🎮 Uso

### Opción 1: Interfaz Gráfica (Recomendado)

```bash
# Terminal 1 - Servidor
java -cp src gui.ServerGUI

# Terminal 2 - Cliente 1
java -cp src gui.ClientGUI

# Terminal 3 - Cliente 2
java -cp src gui.ClientGUI
```

### Opción 2: Línea de Comandos

```bash
# Terminal 1 - Servidor
java -cp src cli.ServerCLI

# Terminal 2 - Cliente 1
java -cp src cli.ClientCLI

# Terminal 3 - Cliente 2
java -cp src cli.ClientCLI
```

## ⚙️ Configuración

- **Host predeterminado**: localhost
- **Puerto predeterminado**: 5555
- **Encoding**: UTF-8

## 📝 Cómo usar

### CLI (Línea de Comandos)

1. Inicia el servidor:
   ```bash
   java -cp src cli.ServerCLI
   ```

2. En otra terminal, inicia un cliente:
   ```bash
   java -cp src cli.ClientCLI
   ```

3. Completa los campos:
   - Host: `localhost`
   - Puerto: `5555`
   - Nombre de usuario: `tu_nombre`

4. Escribe mensajes y presiona Enter
5. Escribe `salir` para desconectar

### GUI (Interfaz Gráfica)

1. **Servidor**: Abre `java -cp src gui.ServerGUI`
   - Se inicia automáticamente
   - Muestra lista de clientes conectados
   - Muestra log de mensajes

2. **Cliente**: Abre `java -cp src gui.ClientGUI`
   - Ingresa host, puerto y nombre de usuario
   - Haz clic en "Conectar"
   - Escribe mensajes en el campo inferior
   - Haz clic en "Enviar" o presiona Enter

## 🔧 Protocolo

- **Tipo**: TCP Socket
- **Puerto**: 5555
- **Formato de mensaje**: `[usuario]: mensaje`
- **Notificaciones del servidor**: `[SERVIDOR] texto`

## 🏗️ Arquitectura

```
Cliente GUI/CLI
      ↓
   Socket TCP
      ↓
   Servidor
      ↓
Broadcast a todos
```

## 📖 Características por componente

### Server.java
- Acepta conexiones de múltiples clientes
- Retransmite mensajes a todos los clientes
- Maneja ClientHandler por cliente

### Client.java
- Cliente básico de consola

### ServerCLI.java
- Interfaz de línea de comandos para el servidor
- Muestra timestamps
- Lista de clientes activos

### ClientCLI.java
- Interfaz de línea de comandos para el cliente
- Muestra timestamps
- Manejo de entrada/salida interactivo

### ServerGUI.java
- Interfaz gráfica con Swing
- Panel de log de mensajes
- Lista visual de clientes conectados
- Botones Iniciar/Detener

### ClientGUI.java
- Interfaz gráfica con Swing
- Panel de conexión configurable
- Área de chat con timestamps
- Campo de entrada de mensajes

## 💡 Ejemplos de uso

### Escenario 1: Chat entre 2 personas en la misma máquina

```
Terminal 1: java -cp src cli.ServerCLI
Terminal 2: java -cp src cli.ClientCLI (Usuario: Juan)
Terminal 3: java -cp src cli.ClientCLI (Usuario: María)

Juan escribe: Hola María
María recibe: [HH:mm:ss] Juan: Hola María
```

### Escenario 2: Interfaz gráfica múltiple

```
Ventana 1: java -cp src gui.ServerGUI
Ventana 2: java -cp src gui.ClientGUI (conectar como Alice)
Ventana 3: java -cp src gui.ClientGUI (conectar como Bob)
```

## 🐛 Solución de problemas

**"Port already in use"**
- El puerto 5555 ya está en uso
- Cambia el puerto en el código o espera a que se libere

**"Connection refused"**
- Asegúrate de que el servidor esté ejecutándose
- Verifica que el host y puerto sean correctos

**"ClassNotFoundException"**
- Verifica que hayas compilado todos los archivos
- Usa `java -cp src nombre.de.Clase`

## 📄 Licencia

MIT

---

**Autor**: SANCHEX10  
**Fecha**: 2026-05-09  
**Lenguaje**: Java  
**Tipo**: Aplicación de Chat con Sockets TCP
